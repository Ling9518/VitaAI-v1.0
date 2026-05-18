-- ============================================================
-- 智慧医院系统 (Smart Hospital System) 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_hospital
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE smart_hospital;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE users (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email           VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password        VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role            ENUM('VISITOR','USER','DOCTOR','ADMIN') DEFAULT 'USER' COMMENT '角色',
    real_name       VARCHAR(100) COMMENT '真实姓名',
    phone           VARCHAR(20) COMMENT '手机号',
    avatar_url      VARCHAR(500) COMMENT '头像URL',
    gender          ENUM('MALE','FEMALE','UNKNOWN') DEFAULT 'UNKNOWN' COMMENT '性别',
    birthday        DATE COMMENT '出生日期',
    doctor_license  VARCHAR(100) COMMENT '医生执照号',
    doctor_title    VARCHAR(50) COMMENT '医生职称',
    doctor_dept     VARCHAR(50) COMMENT '医生科室',
    is_verified     BOOLEAN DEFAULT FALSE COMMENT '是否已验证',
    is_disabled     BOOLEAN DEFAULT FALSE COMMENT '是否禁用',
    last_login_at   DATETIME COMMENT '最后登录时间',
    login_attempts  INT DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until    DATETIME COMMENT '账户锁定截止时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 健康档案表
-- ============================================================
CREATE TABLE health_records (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '档案ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    blood_type          ENUM('A','B','AB','O','UNKNOWN') DEFAULT 'UNKNOWN' COMMENT '血型',
    height              DECIMAL(5,1) COMMENT '身高（cm）',
    weight              DECIMAL(5,1) COMMENT '体重（kg）',
    medical_history     TEXT COMMENT '病史（JSON格式）',
    allergy_history     TEXT COMMENT '过敏史（JSON格式）',
    medication_records  TEXT COMMENT '用药记录（JSON格式）',
    family_history      TEXT COMMENT '家族病史（JSON格式）',
    surgery_history     TEXT COMMENT '手术史（JSON格式）',
    lifestyle           TEXT COMMENT '生活习惯（JSON格式）',
    is_complete         BOOLEAN DEFAULT FALSE COMMENT '档案是否完整',
    completeness_rate   DECIMAL(5,2) DEFAULT 0 COMMENT '完整度百分比',
    last_checkup_date   DATE COMMENT '最近体检日期',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康档案表';

-- ============================================================
-- 3. 疾病表
-- ============================================================
CREATE TABLE diseases (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '疾病ID',
    name            VARCHAR(200) NOT NULL COMMENT '疾病名称',
    alias           VARCHAR(200) COMMENT '疾病别名',
    icd_code        VARCHAR(20) COMMENT 'ICD-10编码',
    cause           TEXT COMMENT '病因',
    symptoms        TEXT COMMENT '症状描述',
    diagnosis       TEXT COMMENT '诊断方法',
    treatment       TEXT COMMENT '治疗方法',
    prevention      TEXT COMMENT '预防措施',
    complications   TEXT COMMENT '并发症',
    classification  VARCHAR(100) COMMENT '疾病分类',
    body_system     VARCHAR(50) COMMENT '人体系统',
    severity        ENUM('MILD','MODERATE','SEVERE') DEFAULT 'MODERATE' COMMENT '严重程度',
    is_infectious   BOOLEAN DEFAULT FALSE COMMENT '是否传染病',
    is_chronic      BOOLEAN DEFAULT FALSE COMMENT '是否慢性病',
    views_count     INT DEFAULT 0 COMMENT '浏览次数',
    favorites_count INT DEFAULT 0 COMMENT '收藏次数',
    created_by      BIGINT COMMENT '创建者ID',
    reviewed_by     BIGINT COMMENT '审核者ID',
    reviewed_at     DATETIME COMMENT '审核时间',
    status          ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'APPROVED' COMMENT '审核状态',
    reject_reason   VARCHAR(500) COMMENT '拒绝原因',
    published_at    DATETIME COMMENT '发布时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id),
    INDEX idx_name (name),
    INDEX idx_classification (classification),
    INDEX idx_body_system (body_system),
    INDEX idx_status (status),
    INDEX idx_views_count (views_count),
    FULLTEXT INDEX ft_diseases (name, symptoms, cause)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疾病表';

-- ============================================================
-- 4. 药品表
-- ============================================================
CREATE TABLE drugs (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '药品ID',
    name            VARCHAR(200) NOT NULL COMMENT '药品名称',
    generic_name    VARCHAR(200) COMMENT '通用名',
    brand_name      VARCHAR(200) COMMENT '商品名',
    search_keywords TEXT COMMENT '搜索关键词',
    efficacy        TEXT COMMENT '功效主治',
    `usage`         TEXT COMMENT '用法用量',
    dosage          TEXT COMMENT '注意事项',
    side_effect     TEXT COMMENT '不良反应',
    contraindication TEXT COMMENT '禁忌',
    drug_type       ENUM('PRESCRIPTION','OTC','HERBAL','BIOLOGIC') DEFAULT 'OTC' COMMENT '药品类型',
    form            VARCHAR(50) COMMENT '剂型',
    specification   VARCHAR(100) COMMENT '规格',
    storage         VARCHAR(200) COMMENT '贮藏条件',
    manufacturer    VARCHAR(200) COMMENT '生产厂家',
    approval_no     VARCHAR(50) COMMENT '批准文号',
    price           DECIMAL(10,2) COMMENT '参考价格',
    views_count     INT DEFAULT 0 COMMENT '浏览次数',
    favorites_count INT DEFAULT 0 COMMENT '收藏次数',
    created_by      BIGINT COMMENT '创建者ID',
    reviewed_by     BIGINT COMMENT '审核者ID',
    reviewed_at     DATETIME COMMENT '审核时间',
    status          ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'APPROVED' COMMENT '审核状态',
    reject_reason   VARCHAR(500) COMMENT '拒绝原因',
    published_at    DATETIME COMMENT '发布时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id),
    INDEX idx_name (name),
    INDEX idx_generic_name (generic_name),
    INDEX idx_drug_type (drug_type),
    INDEX idx_status (status),
    INDEX idx_views_count (views_count),
    FULLTEXT INDEX ft_drugs (name, generic_name, efficacy, search_keywords)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品表';

-- ============================================================
-- 5. 技能表（AI诊断知识库）
-- ============================================================
CREATE TABLE skills (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '技能ID',
    name            VARCHAR(200) NOT NULL COMMENT '技能名称',
    description     TEXT COMMENT '技能描述',
    content         TEXT NOT NULL COMMENT '技能内容（System Prompt）',
    category        VARCHAR(100) COMMENT '技能分类',
    disease_ids     TEXT COMMENT '关联疾病ID列表（JSON）',
    keywords        TEXT COMMENT '触发关键词',
    ai_model        VARCHAR(50) COMMENT '适用的AI模型',
    priority        INT DEFAULT 0 COMMENT '优先级',
    usage_count     INT DEFAULT 0 COMMENT '使用次数',
    success_count   INT DEFAULT 0 COMMENT '成功次数',
    accuracy_rate   DECIMAL(5,2) DEFAULT 0 COMMENT '准确率',
    avg_rating      DECIMAL(3,2) DEFAULT 0 COMMENT '平均评分',
    is_active       BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    version         VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
    created_by      BIGINT COMMENT '创建者ID',
    reviewed_by     BIGINT COMMENT '审核者ID',
    status          ENUM('DRAFT','PENDING','APPROVED','DEPRECATED') DEFAULT 'APPROVED' COMMENT '状态',
    published_at    DATETIME COMMENT '发布时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id),
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_is_active (is_active),
    INDEX idx_usage_count (usage_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能表';

-- ============================================================
-- 6. 诊断记录表
-- ============================================================
CREATE TABLE diagnosis_records (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '诊断记录ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    conversation_id     VARCHAR(100) NOT NULL COMMENT '对话会话ID',
    health_record_id    BIGINT COMMENT '参考的健康档案ID',
    symptom_summary     TEXT COMMENT '症状摘要',
    symptoms_detail     TEXT COMMENT '症状详情（JSON）',
    ai_analysis         TEXT COMMENT 'AI分析结果',
    suggested_diseases  TEXT COMMENT '可能疾病（JSON数组）',
    suggested_drugs     TEXT COMMENT '建议药品（JSON数组）',
    severity_level      ENUM('LOW','MEDIUM','HIGH','CRITICAL') COMMENT '严重程度',
    advice              TEXT COMMENT '建议内容',
    warning_text        TEXT COMMENT '警告信息',
    needs_hospital      BOOLEAN DEFAULT FALSE COMMENT '是否建议就医',
    report_url          VARCHAR(500) COMMENT '诊断报告URL',
    conversation_summary TEXT COMMENT '对话摘要',
    message_count       INT DEFAULT 0 COMMENT '对话轮次',
    total_tokens        INT DEFAULT 0 COMMENT '消耗Token数',
    feedback_accuracy   ENUM('ACCURATE','MOSTLY_ACCURATE','INACCURATE','PENDING') DEFAULT 'PENDING' COMMENT '准确性反馈',
    feedback_detail     TEXT COMMENT '反馈详情',
    feedback_at         DATETIME COMMENT '反馈时间',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (health_record_id) REFERENCES health_records(id),
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_severity_level (severity_level),
    INDEX idx_feedback_accuracy (feedback_accuracy),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='诊断记录表';

-- ============================================================
-- 7. AI对话记录表
-- ============================================================
CREATE TABLE ai_conversations (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '对话ID',
    diagnosis_record_id BIGINT COMMENT '关联诊断记录ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    sender_type         ENUM('USER','AI','SYSTEM') NOT NULL COMMENT '发送者类型',
    message_type        ENUM('TEXT','IMAGE','AUDIO','FILE') DEFAULT 'TEXT' COMMENT '消息类型',
    content             TEXT COMMENT '消息内容',
    image_url           VARCHAR(500) COMMENT '图片URL',
    skills_triggered    TEXT COMMENT '触发的技能（JSON）',
    ai_model_used       VARCHAR(50) COMMENT '使用的AI模型',
    tokens_used         INT DEFAULT 0 COMMENT '消耗Token数',
    latency_ms          INT DEFAULT 0 COMMENT '响应延迟（毫秒）',
    is_medical_question BOOLEAN DEFAULT TRUE COMMENT '是否医疗相关',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (diagnosis_record_id) REFERENCES diagnosis_records(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_diagnosis_record_id (diagnosis_record_id),
    INDEX idx_user_id (user_id),
    INDEX idx_sender_type (sender_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- ============================================================
-- 8. 用户收藏表
-- ============================================================
CREATE TABLE user_favorites (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    item_type   ENUM('DISEASE','DRUG','ARTICLE','DOCTOR') NOT NULL COMMENT '收藏类型',
    item_id     BIGINT NOT NULL COMMENT '收藏项ID',
    notes       VARCHAR(500) COMMENT '备注',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_user_item (user_id, item_type, item_id),
    INDEX idx_user_id (user_id),
    INDEX idx_item (item_type, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ============================================================
-- 9. 症状自测表
-- ============================================================
CREATE TABLE symptom_assessments (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自测ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    assessment_data     TEXT NOT NULL COMMENT '自测数据（JSON）',
    symptom_categories  TEXT COMMENT '选择的症状分类（JSON数组）',
    selected_symptoms   TEXT COMMENT '选择的症状（JSON数组）',
    duration            VARCHAR(50) COMMENT '症状持续时间',
    severity            ENUM('MILD','MODERATE','SEVERE','CRITICAL') DEFAULT 'MODERATE' COMMENT '严重程度',
    preliminary_result  TEXT COMMENT '初步评估结果',
    suggested_actions   TEXT COMMENT '建议行动',
    linked_diagnosis_id BIGINT COMMENT '关联的诊断记录ID',
    status              ENUM('IN_PROGRESS','COMPLETED','ABANDONED') DEFAULT 'IN_PROGRESS' COMMENT '状态',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    completed_at        DATETIME COMMENT '完成时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (linked_diagnosis_id) REFERENCES diagnosis_records(id),
    INDEX idx_user_id (user_id),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='症状自测表';

-- ============================================================
-- 10. 症状分类表
-- ============================================================
CREATE TABLE symptom_categories (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id       BIGINT COMMENT '父分类ID',
    name            VARCHAR(100) NOT NULL COMMENT '分类名称',
    code            VARCHAR(50) NOT NULL COMMENT '分类代码',
    description     VARCHAR(500) COMMENT '分类描述',
    icon            VARCHAR(100) COMMENT '图标',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    is_active       BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES symptom_categories(id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='症状分类表';

-- ============================================================
-- 11. 科研成果表
-- ============================================================
CREATE TABLE research_results (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成果ID',
    title           VARCHAR(500) NOT NULL COMMENT '成果标题',
    abstract        TEXT COMMENT '摘要',
    content         TEXT COMMENT '详细内容',
    research_type   ENUM('PAPER','CASE_STUDY','GUIDELINE','REVIEW') NOT NULL COMMENT '成果类型',
    disease_ids     TEXT COMMENT '关联疾病ID（JSON）',
    keywords        VARCHAR(500) COMMENT '关键词',
    authors         TEXT COMMENT '作者信息（JSON）',
    publication_date DATE COMMENT '发表日期',
    journal         VARCHAR(200) COMMENT '期刊/会议',
    doi             VARCHAR(100) COMMENT 'DOI',
    attachment_url  VARCHAR(500) COMMENT '附件URL',
    views_count     INT DEFAULT 0 COMMENT '浏览次数',
    created_by      BIGINT COMMENT '创建者ID',
    reviewed_by     BIGINT COMMENT '审核者ID',
    status          ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING' COMMENT '审核状态',
    reject_reason   VARCHAR(500) COMMENT '拒绝原因',
    published_at    DATETIME COMMENT '发布时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id),
    INDEX idx_title (title),
    INDEX idx_research_type (research_type),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科研成果表';

-- ============================================================
-- 12. 内容审核表
-- ============================================================
CREATE TABLE content_reviews (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核ID',
    content_type    ENUM('DISEASE','DRUG','SKILL','RESEARCH','CORRECTION') NOT NULL COMMENT '内容类型',
    content_id      BIGINT NOT NULL COMMENT '内容ID',
    submitter_id    BIGINT NOT NULL COMMENT '提交者ID',
    reviewer_id     BIGINT COMMENT '审核者ID',
    action          ENUM('APPROVE','REJECT','MODIFY','RETURN') COMMENT '审核动作',
    review_comments TEXT COMMENT '审核意见',
    reject_reason   VARCHAR(500) COMMENT '拒绝原因',
    priority        ENUM('LOW','NORMAL','HIGH','URGENT') DEFAULT 'NORMAL' COMMENT '优先级',
    status          ENUM('PENDING','IN_REVIEW','APPROVED','REJECTED','MODIFIED') DEFAULT 'PENDING' COMMENT '状态',
    submitted_at    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    reviewed_at     DATETIME COMMENT '审核时间',
    completed_at    DATETIME COMMENT '完成时间',
    FOREIGN KEY (submitter_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    INDEX idx_content (content_type, content_id),
    INDEX idx_submitter (submitter_id),
    INDEX idx_reviewer (reviewer_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容审核表';

-- ============================================================
-- 13. 用户反馈表
-- ============================================================
CREATE TABLE feedback (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
    user_id         BIGINT COMMENT '用户ID',
    type            ENUM('BUG','FEATURE','COMPLAINT','COMPLIMENT','OTHER') NOT NULL COMMENT '反馈类型',
    subject         VARCHAR(200) NOT NULL COMMENT '反馈主题',
    content         TEXT NOT NULL COMMENT '反馈内容',
    contact_email   VARCHAR(100) COMMENT '联系邮箱',
    status          ENUM('SUBMITTED','IN_PROGRESS','RESOLVED','CLOSED') DEFAULT 'SUBMITTED' COMMENT '状态',
    handler_id      BIGINT COMMENT '处理人ID',
    handler_notes   TEXT COMMENT '处理备注',
    reply_content   TEXT COMMENT '回复内容',
    replied_at      DATETIME COMMENT '回复时间',
    rating          INT COMMENT '满意度评分（1-5）',
    ip_address      VARCHAR(50) COMMENT 'IP地址',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (handler_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- ============================================================
-- 14. 操作审计日志表
-- ============================================================
CREATE TABLE audit_logs (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT COMMENT '操作用户ID',
    username        VARCHAR(50) COMMENT '操作用户名',
    ip_address      VARCHAR(50) COMMENT 'IP地址',
    user_agent      VARCHAR(500) COMMENT '浏览器信息',
    module          VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation       VARCHAR(100) NOT NULL COMMENT '操作类型',
    operation_desc  VARCHAR(500) COMMENT '操作描述',
    request_method  VARCHAR(10) COMMENT '请求方法',
    request_url     VARCHAR(500) COMMENT '请求URL',
    request_body    TEXT COMMENT '请求体',
    response_code   INT COMMENT '响应码',
    execution_time  INT COMMENT '执行时间（毫秒）',
    trace_id        VARCHAR(100) COMMENT '追踪ID',
    error_message   TEXT COMMENT '错误信息',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_operation (operation),
    INDEX idx_created_at (created_at),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- ============================================================
-- 15. 疾病药品关联表
-- ============================================================
CREATE TABLE disease_drug_rel (
    disease_id      BIGINT NOT NULL COMMENT '疾病ID',
    drug_id         BIGINT NOT NULL COMMENT '药品ID',
    relationship    ENUM('RECOMMENDED','CONTRAINDICATED','CAUTION') DEFAULT 'RECOMMENDED' COMMENT '关联关系',
    description     VARCHAR(200) COMMENT '关联描述',
    evidence_level  VARCHAR(20) COMMENT '证据级别',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (disease_id, drug_id),
    FOREIGN KEY (disease_id) REFERENCES diseases(id) ON DELETE CASCADE,
    FOREIGN KEY (drug_id) REFERENCES drugs(id) ON DELETE CASCADE,
    INDEX idx_drug_id (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疾病药品关联表';

-- ============================================================
-- 16. 疾病技能关联表
-- ============================================================
CREATE TABLE disease_skill_rel (
    disease_id      BIGINT NOT NULL COMMENT '疾病ID',
    skill_id        BIGINT NOT NULL COMMENT '技能ID',
    weight          DECIMAL(3,2) DEFAULT 1.00 COMMENT '权重',
    match_keywords  TEXT COMMENT '匹配关键词',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (disease_id, skill_id),
    FOREIGN KEY (disease_id) REFERENCES diseases(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    INDEX idx_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疾病技能关联表';

-- ============================================================
-- 17. 患者医生关联表
-- ============================================================
CREATE TABLE patient_doctor_rel (
    patient_id      BIGINT NOT NULL COMMENT '患者ID',
    doctor_id       BIGINT NOT NULL COMMENT '医生ID',
    authorization   ENUM('PENDING','APPROVED','REVOKED') DEFAULT 'PENDING' COMMENT '授权状态',
    authorized_at   DATETIME COMMENT '授权时间',
    revoke_reason   VARCHAR(200) COMMENT '撤销原因',
    notes           TEXT COMMENT '备注',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (patient_id, doctor_id),
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_doctor_id (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者医生关联表';

-- ============================================================
-- 18. 邮箱验证码表
-- ============================================================
CREATE TABLE email_verifications (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '验证ID',
    email           VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    code            VARCHAR(10) NOT NULL COMMENT '验证码',
    type            ENUM('REGISTER','LOGIN','PASSWORD_RESET') NOT NULL COMMENT '验证类型',
    expires_at      DATETIME NOT NULL COMMENT '过期时间',
    is_used         BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_email (email),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- ============================================================
-- 19. 用户消息表
-- ============================================================
CREATE TABLE messages (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    user_id         BIGINT NOT NULL COMMENT '发送者用户ID',
    title           VARCHAR(200) NOT NULL COMMENT '消息标题',
    content         TEXT NOT NULL COMMENT '消息内容',
    status          ENUM('UNREAD','READ','REPLIED') DEFAULT 'UNREAD' COMMENT '消息状态',
    reply           TEXT COMMENT '回复内容',
    replied_by      BIGINT COMMENT '回复者ID',
    replied_at      DATETIME COMMENT '回复时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (replied_by) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息表';

-- ============================================================
-- 20. 联系留言表
-- ============================================================
CREATE TABLE contact_messages (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '留言ID',
    name            VARCHAR(100) NOT NULL COMMENT '联系人姓名',
    email           VARCHAR(100) NOT NULL COMMENT '联系人邮箱',
    subject         VARCHAR(200) NOT NULL COMMENT '留言主题',
    content         TEXT NOT NULL COMMENT '留言内容',
    is_read         BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    is_replied      BOOLEAN DEFAULT FALSE COMMENT '是否已回复',
    replied_at      DATETIME COMMENT '回复时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='联系留言表';

-- ============================================================
-- 21. 初始化症状分类数据
-- ============================================================
INSERT INTO symptom_categories (name, code, description, sort_order) VALUES
('呼吸系统', 'RESPIRATORY', '呼吸系统相关症状', 1),
('消化系统', 'DIGESTIVE', '消化系统相关症状', 2),
('循环系统', 'CARDIOVASCULAR', '循环系统相关症状', 3),
('神经系统', 'NERVOUS', '神经系统相关症状', 4),
('运动系统', 'MUSCULOSKELETAL', '运动系统相关症状', 5),
('皮肤系统', 'DERMATOLOGICAL', '皮肤系统相关症状', 6),
('五官系统', 'ENT', '眼耳鼻喉口相关症状', 7),
('泌尿系统', 'URINARY', '泌尿系统相关症状', 8);

-- ============================================================
-- 22. 初始化管理员账号 (密码: admin123)
-- ============================================================
INSERT INTO users (username, email, password, role, real_name, is_verified)
VALUES ('admin', 'admin@vitaai.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', '系统管理员', TRUE);
