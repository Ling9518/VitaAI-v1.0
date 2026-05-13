#!/usr/bin/env python3
"""Parse Drug-Disease markdown files and import into MySQL database."""

import re
import os
import sys
import json
from datetime import datetime
from collections import OrderedDict

# MySQL connection
import mysql.connector

DB_CONFIG = {
    'host': '127.0.0.1',
    'port': 3306,
    'user': 'root',
    'password': '689518',
    'database': 'smart_hospital',
    'charset': 'utf8mb4'
}

BASE_DIR = r'C:\Users\ASUS\Desktop\VitaAI4\Drug-Disease'

# Body system mapping from Chinese section names
BODY_SYSTEM_MAP = {
    '心血管': 'CARDIOVASCULAR',
    '内分泌': 'ENDOCRINE',
    '消化': 'DIGESTIVE',
    '呼吸': 'RESPIRATORY',
    '神经': 'NERVOUS',
    '皮肤': 'DERMATOLOGY',
    '骨科': 'ORTHOPEDIC_RHEUMATOLOGY',
    '风湿': 'ORTHOPEDIC_RHEUMATOLOGY',
    '泌尿': 'URINARY',
    '血液': 'HEMATOLOGY',
    '肿瘤': 'ONCOLOGY',
    '儿科': 'PEDIATRICS',
    '妇产': 'OBSTETRICS_GYNECOLOGY',
    '精神': 'PSYCHIATRY',
    '感染': 'INFECTIOUS_DISEASE',
    '急诊': 'EMERGENCY',
    '中毒': 'EMERGENCY',
    '眼科': 'OPHTHALMOLOGY',
    '耳鼻喉': 'OTOLARYNGOLOGY',
    '口腔': 'DENTAL',
}

def get_db():
    return mysql.connector.connect(**DB_CONFIG)

def clean_text(text):
    """Clean markdown text for DB insertion."""
    if not text:
        return ''
    text = text.strip()
    # Remove markdown formatting
    text = re.sub(r'\*\*(.+?)\*\*', r'\1', text)
    text = re.sub(r'\*(.+?)\*', r'\1', text)
    text = re.sub(r'`([^`]+)`', r'\1', text)
    # Remove leading bullet markers
    text = re.sub(r'^[-*]\s+', '', text, flags=re.MULTILINE)
    # Collapse multiple newlines
    text = re.sub(r'\n{3,}', '\n\n', text)
    return text.strip()

def detect_body_system(section_name):
    """Detect body system from section name."""
    for key, value in BODY_SYSTEM_MAP.items():
        if key in section_name:
            return value
    return 'OTHER'

def detect_severity(text):
    """Detect severity from text content."""
    text_lower = (text or '').lower()
    severe_keywords = ['急性', '重症', '危重', '梗死', '卒中', '衰竭', '恶性', '休克', '夹层', '晚期', '转移']
    mild_keywords = ['轻度', '轻症', '普通感冒', '季节性', '过敏性鼻炎', '口腔溃疡', '干眼', '便秘']

    for kw in severe_keywords:
        if kw in text:
            return 'SEVERE'
    for kw in mild_keywords:
        if kw in text:
            return 'MILD'
    return 'MODERATE'

def parse_main_volume(filepath):
    """Parse the main disease-drug volume with bullet-list format."""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    diseases = []
    current_section = ''

    # Split by disease entries (### Pattern)
    # First find all section headers (##)
    sections = re.findall(r'^## (.+)$', content, re.MULTILINE)

    # Split by ### disease entries
    disease_blocks = re.split(r'\n---\n', content)

    for block in disease_blocks:
        # Always check for section header first (may be in same block as disease)
        sec_match = re.search(r'^## (.+)$', block, re.MULTILINE)
        if sec_match:
            current_section = sec_match.group(1).strip()

        # Find disease title
        title_match = re.search(r'^### (.+)$', block, re.MULTILINE)
        if not title_match:
            continue

        title = title_match.group(1).strip()

        # Parse name and alias
        # Format: "高血压 | 高血压" or "冠心病（冠状动脉粥样硬化性心脏病） | 冠心病"
        # OR "乳腺癌 | Breast Cancer" (with English name after |)
        parts = re.split(r'\s*\|\s*', title, maxsplit=1)
        english_name = ''
        if len(parts) == 2:
            name = parts[0].strip()
            second = parts[1].strip()
            # Determine if second part is English (contains Latin chars) or Chinese alias
            if re.search(r'[a-zA-Z]', second):
                english_name = second
                # Extract alias from Chinese parentheses in name
                alias_match = re.search(r'（(.+?)）', name)
                if alias_match:
                    alias = alias_match.group(1).strip()
                    name = name.replace(f'（{alias}）', '').strip()
                else:
                    alias = ''
            else:
                alias = second
        else:
            name = parts[0].strip()
            # Extract Chinese alias from parentheses
            alias_match = re.search(r'（(.+?)）', name)
            if alias_match:
                alias = alias_match.group(1).strip()
                name = name.replace(f'（{alias}）', '').strip()
            else:
                alias = ''

        # Parse fields
        icd_match = re.search(r'\*\*ICD-11编码[：:]\*\*\s*(.+)', block)
        icd_code = icd_match.group(1).strip() if icd_match else ''

        overview_match = re.search(r'\*\*疾病概述[：:]\*\*\s*(.+?)(?=\n\n\*\*|$)', block, re.DOTALL)
        cause = overview_match.group(1).strip() if overview_match else ''

        symptoms_match = re.search(r'\*\*症状[：:]\*\*\s*(.+?)(?=\n\n\*\*|$)', block, re.DOTALL)
        symptoms = symptoms_match.group(1).strip() if symptoms_match else ''

        diagnosis_match = re.search(r'\*\*诊断[：:]\*\*\s*(.+?)(?=\n\n\*\*|$)', block, re.DOTALL)
        diagnosis = diagnosis_match.group(1).strip() if diagnosis_match else ''

        # Parse drug treatment section
        drug_treatment = ''
        drug_section = re.search(r'\*\*药物治疗[：:]\*\*\s*(.+?)(?=\*\*药物分类|\*\*作用机制|\*\*非药物治疗)', block, re.DOTALL)
        if drug_section:
            drug_treatment = drug_section.group(1).strip()

        drug_category = ''
        cat_match = re.search(r'\*\*药物分类[：:]\*\*\s*(.+)', block)
        if cat_match:
            drug_category = cat_match.group(1).strip()

        mechanism = ''
        mech_match = re.search(r'\*\*作用机制[：:]\*\*\s*(.+?)(?=\n\n\*\*|\n\*\*|$)', block, re.DOTALL)
        if mech_match:
            mechanism = mech_match.group(1).strip()

        non_drug = ''
        non_drug_match = re.search(r'\*\*非药物治疗[：:]\*\*\s*(.+?)(?=\n\n\*\*|\n\*\*|$)', block, re.DOTALL)
        if non_drug_match:
            non_drug = non_drug_match.group(1).strip()

        contraindication = ''
        contra_match = re.search(r'\*\*禁忌症与注意事项[：:]\*\*\s*(.+?)(?=\n\n\*\*|\n\*\*|$)', block, re.DOTALL)
        if contra_match:
            contraindication = contra_match.group(1).strip()

        guidelines = ''
        guide_match = re.search(r'\*\*临床指南参考[：:]\*\*\s*(.+?)(?=\n---|\n$|$)', block, re.DOTALL)
        if guide_match:
            guidelines = guide_match.group(1).strip()

        # Combine treatment from drug + non-drug
        treatment_parts = []
        if drug_treatment:
            treatment_parts.append('【药物治疗】\n' + drug_treatment)
        if non_drug:
            treatment_parts.append('【非药物治疗】\n' + non_drug)
        treatment = '\n\n'.join(treatment_parts)

        # Build complications from guidelines (not a separate field in source)
        complications = ''

        # Determine body system
        body_system = detect_body_system(current_section)

        # Determine severity from overview
        severity = detect_severity(cause)

        # Determine infectious/chronic
        is_infectious = 1 if any(kw in (cause + name) for kw in ['感染', '病毒', '细菌', '真菌', '微生物']) else 0
        is_chronic = 1 if any(kw in (cause + name) for kw in ['慢性', '长期', '持续性', '进行性']) else 0

        disease = {
            'name': clean_text(name)[:200],
            'alias': clean_text(alias)[:200] if alias else '',
            'icd_code': icd_code[:20],
            'cause': clean_text(cause),
            'symptoms': clean_text(symptoms) if symptoms else '',
            'diagnosis': clean_text(diagnosis) if diagnosis else '',
            'treatment': clean_text(treatment),
            'prevention': clean_text(non_drug) if non_drug else '',
            'complications': clean_text(complications) if complications else '',
            'classification': current_section[:100] if current_section else '',
            'body_system': body_system,
            'severity': severity,
            'is_infectious': is_infectious,
            'is_chronic': is_chronic,
            'status': 'APPROVED',
            'created_by': 1,
            'views_count': 0,
            'favorites_count': 0,
            # Raw drug info for creating drug entries
            '_drug_category': drug_category,
            '_mechanism': mechanism,
            '_contraindication': contraindication,
            '_guidelines': guidelines,
        }
        diseases.append(disease)

    return diseases

def parse_supplementary_volume(filepath):
    """Parse the supplementary volume with table-format drug data."""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    diseases = []
    current_section = ''

    disease_blocks = re.split(r'\n---\n', content)

    for block in disease_blocks:
        # Always check for section header first (may be in same block as disease)
        sec_match = re.search(r'^## (.+)$', block, re.MULTILINE)
        if sec_match:
            current_section = sec_match.group(1).strip()

        title_match = re.search(r'^### \d+\.\s*(.+)$', block, re.MULTILINE)
        if not title_match:
            continue

        title = title_match.group(1).strip()

        # Parse name and English name
        # Format: "乳腺癌 | Breast Cancer"
        parts = re.split(r'\s*\|\s*', title, maxsplit=1)
        if len(parts) == 2:
            name = parts[0].strip()
            english_name = parts[1].strip()
        else:
            name = parts[0].strip()
            english_name = ''

        # Remove any markdown formatting from name
        name = re.sub(r'\*\*|__', '', name)
        alias = english_name

        # Parse fields
        icd_match = re.search(r'\*\*ICD-11编码[：:]\*\*\s*(.+)', block)
        icd_code = icd_match.group(1).strip() if icd_match else ''

        overview_match = re.search(r'\*\*疾病概述[：:]\*\*\s*(.+?)(?=\n\n\*\*|$)', block, re.DOTALL)
        cause = overview_match.group(1).strip() if overview_match else ''

        # Parse drug treatment (table format in supplementary volume)
        drug_treatment = ''
        drug_section_match = re.search(r'\*\*药物治疗[：:]\*\*\s*\n+(.+?)(?=\n\*\*非药物治疗|\n\*\*禁忌症)', block, re.DOTALL)
        if drug_section_match:
            drug_treatment = drug_section_match.group(1).strip()

        non_drug = ''
        non_drug_match = re.search(r'\*\*非药物治疗[：:]\*\*\s*\n*(.+?)(?=\n\*\*禁忌症|\n\*\*临床指南|$)', block, re.DOTALL)
        if non_drug_match:
            non_drug = non_drug_match.group(1).strip()

        contraindication = ''
        contra_match = re.search(r'\*\*禁忌症与注意事项[：:]\*\*\s*\n*(.+?)(?=\n\*\*临床指南|$)', block, re.DOTALL)
        if contra_match:
            contraindication = contra_match.group(1).strip()

        guidelines = ''
        guide_match = re.search(r'\*\*临床指南参考[：:]\*\*\s*\n*(.+?)(?=\n---|\n$|$)', block, re.DOTALL)
        if guide_match:
            guidelines = guide_match.group(1).strip()

        # Build treatment
        treatment_parts = []
        if drug_treatment:
            treatment_parts.append('【药物治疗】\n' + drug_treatment)
        if non_drug:
            treatment_parts.append('【非药物治疗】\n' + non_drug)
        treatment = '\n\n'.join(treatment_parts)

        # Build prevention from non-drug
        prevention = non_drug if non_drug else ''

        body_system = detect_body_system(current_section)
        severity = detect_severity(cause)
        is_infectious = 1 if any(kw in (cause + name) for kw in ['感染', '病毒', '细菌', '真菌', '微生物']) else 0
        is_chronic = 1 if any(kw in (cause + name) for kw in ['慢性', '长期', '持续性', '进行性']) else 0

        disease = {
            'name': clean_text(name)[:200],
            'alias': clean_text(alias)[:200] if alias else '',
            'icd_code': icd_code[:20],
            'cause': clean_text(cause),
            'symptoms': '',
            'diagnosis': '',
            'treatment': clean_text(treatment),
            'prevention': clean_text(prevention),
            'complications': '',
            'classification': current_section[:100] if current_section else '',
            'body_system': body_system,
            'severity': severity,
            'is_infectious': is_infectious,
            'is_chronic': is_chronic,
            'status': 'APPROVED',
            'created_by': 1,
            'views_count': 0,
            'favorites_count': 0,
            '_guidelines': guidelines,
            '_contraindication': contraindication,
        }
        diseases.append(disease)

    return diseases

def parse_drug_manual(filepath):
    """Parse the drug quick reference manual."""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    drugs = []
    current_section = ''

    drug_blocks = re.split(r'\n---\n', content)

    for block in drug_blocks:
        # Match both ### N. Name and #### N. Name patterns
        title_match = re.search(r'^#{3,4}\s*\d+\.\s*(.+)$', block, re.MULTILINE)
        if not title_match:
            sec_match = re.search(r'^## (.+)$', block, re.MULTILINE)
            if sec_match:
                current_section = sec_match.group(1).strip()
            continue

        title = title_match.group(1).strip()

        # Parse name and English name
        # Format: "对乙酰氨基酚 (Paracetamol)" or "阿莫西林 (Amoxicillin)"
        paren_match = re.match(r'(.+?)\s*\((.+?)\)\s*$', title)
        if paren_match:
            name = paren_match.group(1).strip()
            english_name = paren_match.group(2).strip()
        else:
            # Format: "Name | English"
            parts = re.split(r'\s*\|\s*', title, maxsplit=1)
            if len(parts) == 2:
                name = parts[0].strip()
                english_name = parts[1].strip()
            else:
                name = parts[0].strip()
                english_name = ''

        # Parse fields
        category_match = re.search(r'\*\*药物分类[：:]\*\*\s*(.+)', block)
        drug_category = category_match.group(1).strip() if category_match else ''

        indication_match = re.search(r'\*\*适应症[：:]\*\*\s*\n*(.+?)(?=\n\n\*\*用法|\n\*\*用法)', block, re.DOTALL)
        efficacy = indication_match.group(1).strip() if indication_match else ''

        usage_match = re.search(r'\*\*用法用量[：:]\*\*\s*\n*(.+?)(?=\n\n\*\*不良反应|\n\*\*不良反应|\n\n\*\*禁忌|\n\*\*禁忌)', block, re.DOTALL)
        usage = usage_match.group(1).strip() if usage_match else ''

        side_effect_match = re.search(r'\*\*不良反应[：:]\*\*\s*\n*(.+?)(?=\n\n\*\*禁忌|\n\*\*禁忌|\n\n\*\*注意|\n\*\*注意)', block, re.DOTALL)
        side_effect = side_effect_match.group(1).strip() if side_effect_match else ''

        contraindication_match = re.search(r'\*\*禁忌症[：:]\*\*\s*\n*(.+?)(?=\n\n\*\*注意|\n\*\*注意|\n---|$)', block, re.DOTALL)
        contraindication = contraindication_match.group(1).strip() if contraindication_match else ''

        notes_match = re.search(r'\*\*注意事项[：:]\*\*\s*\n*(.+?)(?=\n---|$)', block, re.DOTALL)
        notes = notes_match.group(1).strip() if notes_match else ''

        # Determine drug type
        drug_type = 'OTC'
        if any(kw in (drug_category + name + efficacy) for kw in ['抗生素', '抗菌', '抗病毒', '抗真菌', '处方']):
            drug_type = 'PRESCRIPTION'
        if any(kw in drug_category for kw in ['生物', '疫苗', '抗体']):
            drug_type = 'BIOLOGIC'
        if any(kw in (drug_category + name) for kw in ['中成药', '中药', '中草药']):
            drug_type = 'HERBAL'

        # Combine contraindication and notes
        full_contraindication = ''
        if contraindication:
            full_contraindication += '【禁忌症】\n' + contraindication
        if notes:
            if full_contraindication:
                full_contraindication += '\n\n【注意事项】\n' + notes
            else:
                full_contraindication = '【注意事项】\n' + notes

        drug = {
            'name': clean_text(name)[:200],
            'generic_name': clean_text(english_name)[:200] if english_name else clean_text(name)[:200],
            'brand_name': '',
            'efficacy': clean_text(efficacy),
            'usage': clean_text(usage),
            'dosage': clean_text(usage),  # Same field for drugs
            'side_effect': clean_text(side_effect),
            'contraindication': clean_text(full_contraindication),
            'drug_type': drug_type,
            'form': '',
            'specification': '',
            'storage': '',
            'manufacturer': '',
            'approval_no': '',
            'search_keywords': clean_text(drug_category),
            'status': 'APPROVED',
            'created_by': 1,
            'views_count': 0,
            'favorites_count': 0,
            'price': None,
        }
        drugs.append(drug)

    return drugs

def extract_drugs_from_diseases(diseases):
    """Extract drug categories from disease data to create drug entries."""
    drugs = []
    seen_names = set()

    # Get existing drug names from DB
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT name FROM drugs")
    existing = set(row[0] for row in cursor.fetchall())
    cursor.close()
    db.close()

    seen_names.update(existing)

    for disease in diseases:
        category_str = disease.get('_drug_category', '')
        mechanism_str = disease.get('_mechanism', '')
        contra_str = disease.get('_contraindication', '')
        disease_name = disease.get('name', '')

        if not category_str:
            continue

        # Parse drug categories
        categories = [c.strip() for c in re.split(r'[、，,;；]', category_str) if c.strip()]

        for cat in categories:
            # Clean up category name
            cat = re.sub(r'（.+?）', '', cat)
            cat = re.sub(r'\(.+?\)', '', cat)
            cat = cat.strip()

            if not cat or len(cat) > 100:
                continue

            if cat in seen_names:
                continue
            seen_names.add(cat)

            drugs.append({
                'name': cat[:200],
                'generic_name': cat[:200],
                'efficacy': f'用于治疗{disease_name}'[:65535],
                'contraindication': clean_text(contra_str)[:65535] if contra_str else '',
                'drug_type': 'PRESCRIPTION',
                'status': 'APPROVED',
                'created_by': 1,
                'views_count': 0,
                'favorites_count': 0,
                'search_keywords': f'{cat}, {disease_name}',
            })

    return drugs

def insert_diseases(diseases):
    """Insert diseases into database."""
    db = get_db()
    cursor = db.cursor()

    # Get existing disease names
    cursor.execute("SELECT name FROM diseases")
    existing = set(row[0] for row in cursor.fetchall())

    inserted = 0
    skipped = 0

    sql = """
    INSERT INTO diseases
    (name, alias, icd_code, cause, symptoms, diagnosis, treatment, prevention,
     complications, classification, body_system, severity, is_infectious, is_chronic,
     views_count, favorites_count, status, created_by, created_at)
    VALUES
    (%(name)s, %(alias)s, %(icd_code)s, %(cause)s, %(symptoms)s, %(diagnosis)s,
     %(treatment)s, %(prevention)s, %(complications)s, %(classification)s,
     %(body_system)s, %(severity)s, %(is_infectious)s, %(is_chronic)s,
     %(views_count)s, %(favorites_count)s, %(status)s, %(created_by)s, NOW())
    """

    for d in diseases:
        name = d['name']
        if name in existing:
            skipped += 1
            continue

        try:
            cursor.execute(sql, {
                'name': d['name'],
                'alias': d.get('alias', ''),
                'icd_code': d.get('icd_code', ''),
                'cause': d.get('cause', ''),
                'symptoms': d.get('symptoms', ''),
                'diagnosis': d.get('diagnosis', ''),
                'treatment': d.get('treatment', ''),
                'prevention': d.get('prevention', ''),
                'complications': d.get('complications', ''),
                'classification': d.get('classification', ''),
                'body_system': d.get('body_system', 'OTHER'),
                'severity': d.get('severity', 'MODERATE'),
                'is_infectious': d.get('is_infectious', 0),
                'is_chronic': d.get('is_chronic', 0),
                'views_count': 0,
                'favorites_count': 0,
                'status': 'APPROVED',
                'created_by': 1,
            })
            inserted += 1
        except Exception as e:
            print(f"  Error inserting disease '{name}': {e}")
            continue

    db.commit()
    cursor.close()
    db.close()
    print(f"  Diseases: {inserted} inserted, {skipped} skipped (already exist)")
    return inserted

def insert_drugs(drugs):
    """Insert drugs into database."""
    db = get_db()
    cursor = db.cursor()

    cursor.execute("SELECT name FROM drugs")
    existing = set(row[0] for row in cursor.fetchall())

    inserted = 0
    skipped = 0

    sql = """
    INSERT INTO drugs
    (name, generic_name, brand_name, efficacy, `usage`, dosage, side_effect,
     contraindication, drug_type, form, specification, storage, manufacturer,
     approval_no, search_keywords, views_count, favorites_count, status, created_by, created_at)
    VALUES
    (%(name)s, %(generic_name)s, %(brand_name)s, %(efficacy)s, %(usage)s, %(dosage)s,
     %(side_effect)s, %(contraindication)s, %(drug_type)s, %(form)s, %(specification)s,
     %(storage)s, %(manufacturer)s, %(approval_no)s, %(search_keywords)s,
     %(views_count)s, %(favorites_count)s, %(status)s, %(created_by)s, NOW())
    """

    for d in drugs:
        name = d.get('name', '')
        if not name or name in existing:
            skipped += 1
            continue

        try:
            cursor.execute(sql, {
                'name': d.get('name', '')[:200],
                'generic_name': d.get('generic_name', '')[:200],
                'brand_name': d.get('brand_name', '')[:200],
                'efficacy': d.get('efficacy', ''),
                'usage': d.get('usage', ''),
                'dosage': d.get('dosage', ''),
                'side_effect': d.get('side_effect', ''),
                'contraindication': d.get('contraindication', ''),
                'drug_type': d.get('drug_type', 'OTC'),
                'form': d.get('form', '')[:50],
                'specification': d.get('specification', '')[:100],
                'storage': d.get('storage', '')[:200],
                'manufacturer': d.get('manufacturer', '')[:200],
                'approval_no': d.get('approval_no', '')[:50],
                'search_keywords': d.get('search_keywords', ''),
                'views_count': 0,
                'favorites_count': 0,
                'status': 'APPROVED',
                'created_by': 1,
            })
            inserted += 1
        except Exception as e:
            print(f"  Error inserting drug '{name}': {e}")
            continue

    db.commit()
    cursor.close()
    db.close()
    print(f"  Drugs: {inserted} inserted, {skipped} skipped (already exist)")
    return inserted

def main():
    print("=" * 60)
    print("Drug-Disease Data Parser & Importer")
    print("=" * 60)

    # Check if mysql-connector-python is installed
    try:
        import mysql.connector
    except ImportError:
        print("Installing mysql-connector-python...")
        os.system(f"{sys.executable} -m pip install mysql-connector-python -q")
        import mysql.connector

    all_diseases = []
    all_drugs = []

    # 1. Parse main volume
    main_file = os.path.join(BASE_DIR, '疾病药物与治疗方式大全（增强版）.md')
    if os.path.exists(main_file):
        print(f"\n[1/4] Parsing main volume: {os.path.basename(main_file)}")
        diseases = parse_main_volume(main_file)
        print(f"  Found {len(diseases)} diseases")
        all_diseases.extend(diseases)
    else:
        print(f"Main volume not found: {main_file}")

    # 2. Parse supplementary volume
    supp_file = os.path.join(BASE_DIR, '疾病药物与治疗方式大全（补充卷）.md')
    if os.path.exists(supp_file):
        print(f"\n[2/4] Parsing supplementary volume: {os.path.basename(supp_file)}")
        diseases = parse_supplementary_volume(supp_file)
        print(f"  Found {len(diseases)} diseases")
        all_diseases.extend(diseases)
    else:
        print(f"Supplementary volume not found: {supp_file}")

    # 3. Parse drug manual
    drug_file = os.path.join(BASE_DIR, '常用药品速查手册.md')
    if os.path.exists(drug_file):
        print(f"\n[3/4] Parsing drug manual: {os.path.basename(drug_file)}")
        drugs = parse_drug_manual(drug_file)
        print(f"  Found {len(drugs)} drugs")
        all_drugs.extend(drugs)
    else:
        print(f"Drug manual not found: {drug_file}")

    # Extract drug categories from disease data
    print(f"\n[4/4] Importing to database...")
    print(f"  Total diseases to import: {len(all_diseases)}")

    # Import diseases first
    inserted_diseases = insert_diseases(all_diseases)

    # Extract and import drug categories from diseases
    extracted_drugs = extract_drugs_from_diseases(all_diseases)
    print(f"  Extracted {len(extracted_drugs)} drug categories from disease data")
    all_drugs.extend(extracted_drugs)

    # Import drugs
    print(f"  Total drugs to import: {len(all_drugs)}")
    inserted_drugs = insert_drugs(all_drugs)

    # Show final counts
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT COUNT(*) FROM diseases")
    total_diseases = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM drugs")
    total_drugs = cursor.fetchone()[0]
    cursor.close()
    db.close()

    print(f"\n{'=' * 60}")
    print(f"IMPORT COMPLETE")
    print(f"  Diseases in DB: {total_diseases}")
    print(f"  Drugs in DB: {total_drugs}")
    print(f"{'=' * 60}")

if __name__ == '__main__':
    main()
