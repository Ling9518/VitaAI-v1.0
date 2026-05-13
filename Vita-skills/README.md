# 医学Skills总览

本目录包含三个医生相关的AI技能，用于构建具备丰富医学知识的医疗辅助Agent。

## Skills列表

### 1. medical-diagnosis（医学诊断技能）

**位置**：`./medical-diagnosis/`

**功能**：
- 基于症状的鉴别诊断分析
- 红旗征识别与危急重症预警
- 诊断标准与检查建议
- ICD-11疾病分类检索

**触发场景**：
- 用户描述症状、体征
- 需要疾病诊断分析
- 鉴别诊断咨询

**数据支持**：
- 90+种疾病数据库
- 症状-疾病映射表
- 红旗征识别规则

---

### 2. pharmacotherapy（药物治疗技能）

**位置**：`./pharmacotherapy/`

**功能**：
- 疾病药物治疗方案推荐
- 一线/二线药物选择
- 药物作用机制解释
- 禁忌症与注意事项
- 药物相互作用检测
- 特殊人群用药调整

**触发场景**：
- 药物选择咨询
- 剂量调整需求
- 用药指导请求
- 药物相互作用查询

**数据支持**：
- 124种药物分类索引
- 药物相互作用库
- 特殊人群用药指南

---

### 3. clinical-guidelines（临床指南技能）

**位置**：`./clinical-guidelines/`

**功能**：
- 权威临床指南检索
- 核心推荐提取
- 指南更新对比
- 证据等级解读
- 中外指南差异分析

**触发场景**：
- 查阅诊疗指南
- 循证医学证据查询
- 临床路径参考

**数据支持**：
- 36部临床指南索引
- 证据等级说明
- 指南更新追踪

---

## 数据来源

所有技能数据参照以下权威医学资料：

### 国际权威机构
- **Medscape** - 临床决策支持、药物参考
- **WHO** - 国际疾病分类、诊疗指南
- **ESC** - 欧洲心脏病学会指南
- **AHA/ACC** - 美国心脏协会/心脏病学会指南
- **ADA** - 美国糖尿病协会标准
- **GINA/GOLD** - 哮喘/COPD全球倡议
- **KDIGO** - 肾脏病改善全球预后

### 国内权威机构
- 中华医学会各专科分会
- 中国高血压联盟
- 中国医师协会

---

## 使用方法

### 作为Agent Skill使用

将各skill目录复制到Agent的skills目录下，Agent将自动识别并使用：

```
/your-agent/skills/
├── medical-diagnosis/
│   ├── SKILL.md
│   ├── data/
│   └── references/
├── pharmacotherapy/
│   ├── SKILL.md
│   ├── data/
│   └── references/
└── clinical-guidelines/
    ├── SKILL.md
    ├── data/
    └── references/
```

### 调用示例

**诊断场景**：
```
用户：患者65岁男性，突发胸骨后压榨样疼痛2小时，伴出汗
Agent：[调用medical-diagnosis skill]
       → 识别红旗征：急性冠脉综合征
       → 鉴别诊断：心肌梗死、主动脉夹层、不稳定型心绞痛
       → 建议检查：心电图、心肌标志物
```

**药物治疗场景**：
```
用户：高血压患者，血压155/95mmHg，推荐什么药物？
Agent：[调用pharmacotherapy skill]
       → 一线药物：氨氯地平 5mg qd 或 缬沙坦 80mg qd
       → 作用机制、注意事项
       → 随访建议
```

**指南查询场景**：
```
用户：ESC心衰指南对SGLT2i的推荐是什么？
Agent：[调用clinical-guidelines skill]
       → HFrEF患者推荐SGLT2i [I/A]
       → HFpEF患者建议SGLT2i [IIa/A]
       → 证据来源：DAPA-HF, EMPEROR-Reduced研究
```

---

## 注意事项

### 法律声明
- 所有医学建议仅供参考
- 不能替代执业医师的临床判断
- 最终诊断和治疗决策需由医生确认

### 数据更新
- 医学知识库定期更新
- 参照最新临床指南
- 关注药物安全性更新

### 个体化考量
- 所有建议需结合患者具体情况
- 注意个体差异和特殊情况
- 综合考虑患者偏好和价值观

---

## 文件结构

```
medical-skills/
├── README.md                          # 本文件
├── medical-diagnosis/                 # 诊断技能
│   ├── SKILL.md                       # 技能主文件
│   ├── data/
│   │   └── diseases.json              # 疾病数据库
│   └── references/
│       ├── symptom-analysis.md        # 症状分析指南
│       └── red-flags.md               # 红旗征识别
├── pharmacotherapy/                   # 药物治疗技能
│   ├── SKILL.md                       # 技能主文件
│   ├── data/
│   │   └── drugs-index.json           # 药物索引
│   └── references/
│       ├── drug-interactions.md       # 药物相互作用
│       └── special-populations.md     # 特殊人群用药
└── clinical-guidelines/               # 临床指南技能
    ├── SKILL.md                       # 技能主文件
    ├── data/
    │   └── guidelines-index.json      # 指南索引
    └── references/
        └── evidence-grading.md        # 证据等级说明
```

---

## 版本信息

- **创建日期**：2025年5月
- **数据版本**：基于2024-2025年最新指南
- **疾病覆盖**：90+种常见疾病
- **药物覆盖**：124种药物分类
- **指南覆盖**：36部权威指南

---

## 扩展建议

如需进一步扩展医学Agent能力，可考虑添加：

1. **医学影像解读技能** - X线、CT、MRI解读
2. **检验结果解读技能** - 实验室指标解读
3. **病历书写技能** - 病历格式与内容指导
4. **医学文献检索技能** - PubMed、知网检索
5. **患者教育技能** - 通俗易懂的疾病解释
6. **急救处理技能** - 常见急症处理流程

---

## 联系与反馈

如有问题或建议，请联系开发者。
