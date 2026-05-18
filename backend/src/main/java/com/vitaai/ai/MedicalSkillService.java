package com.vitaai.ai;

import com.vitaai.entity.Skill;
import com.vitaai.entity.User;
import com.vitaai.repository.SkillRepository;
import com.vitaai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MedicalSkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    @Value("${vita-skills.path:../Vita-skills}")
    private String vitaSkillsPath;

    private static final String MEDICAL_SYSTEM_PROMPT = """
        【角色定义】
        你是VitaAI智慧医生，一个基于人工智能的医疗辅助诊断系统。你整合了全球权威医学知识和临床指南，
        能够为用户提供专业的健康咨询和初步诊断建议。

        【核心能力】
        1. 症状分析：基于用户描述的症状进行专业分析
        2. 鉴别诊断：提供可能的疾病列表及概率分析
        3. 用药建议：根据疾病推荐合适的药物方案（需注明仅供参考）
        4. 健康指导：提供饮食、运动、生活方式等建议
        5. 红旗征识别：识别危急重症征兆，提示立即就医

        【医学知识库】
        你具备以下医学领域的专业知识：
        - 心血管系统：高血压、冠心病、心力衰竭、心律失常等
        - 内分泌系统：糖尿病、甲状腺疾病等
        - 消化系统：胃炎、消化性溃疡、肝炎等
        - 呼吸系统：感冒、肺炎、哮喘、COPD等
        - 神经系统：头痛、偏头痛、脑卒中等
        - 皮肤科：湿疹、皮炎、痤疮等
        - 骨科与风湿：关节炎、骨质疏松等
        - 泌尿系统：肾炎、泌尿感染等

        【诊断原则】
        1. 先识别危急重症（红旗征），如有生命危险立即建议就医
        2. 按照"常见病优先、重症不遗漏"原则进行鉴别诊断
        3. 每次提供2-5个可能的诊断，按概率排序
        4. 说明每个诊断的支持点和不支持点
        5. 建议必要的检查项目以明确诊断

        【用药原则】
        1. 区分处方药和非处方药，处方药必须强调需医生处方
        2. 提供药物名称、用法用量、注意事项
        3. 说明可能的副作用和禁忌症
        4. 注意药物相互作用

        【安全边界】
        1. 你必须始终在回复末尾加上："【内容为AI诊断，想要更准确诊断，请去正规医院就诊。】"
        2. 不提供确定的诊断结论，使用"可能"、"建议"等措辞
        3. 不推荐未经批准的药物或疗法
        4. 遇到紧急情况必须首先建议拨打120急救电话
        5. 保护用户隐私，不要求提供可识别个人身份的信息

        【交互风格】
        1. 专业但不冷漠，温暖但不随意
        2. 主动追问关键信息以缩小诊断范围
        3. 使用通俗语言解释专业术语
        4. 结构化呈现诊断信息，便于理解
        """;

    // ---- Skill query ----

    public List<Skill> getActiveSkills() {
        return skillRepository.findByIsActiveTrue();
    }

    public Page<Skill> getAllSkills(int page, int size) {
        return skillRepository.findAll(PageRequest.of(page - 1, size));
    }

    public Skill getSkill(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能不存在"));
    }

    // ---- CRUD ----

    @Transactional
    public Skill createSkill(Skill skill, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        skill.setCreatedBy(user);
        skill.setStatus(Skill.Status.APPROVED);
        skill.setPublishedAt(LocalDateTime.now());
        return skillRepository.save(skill);
    }

    @Transactional
    public Skill updateSkill(Long id, Skill updates) {
        Skill existing = getSkill(id);
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getContent() != null) existing.setContent(updates.getContent());
        if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
        if (updates.getKeywords() != null) existing.setKeywords(updates.getKeywords());
        if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
        if (updates.getPriority() != null) existing.setPriority(updates.getPriority());
        return skillRepository.save(existing);
    }

    @Transactional
    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    // ---- Vita-skills file operations ----

    /**
     * Upload a SKILL.md file to the Vita-skills directory.
     * Saves content as Vita-skills/{name}/SKILL.md
     */
    public void uploadSkillFile(String name, String content) throws IOException {
        Path dir = Paths.get(vitaSkillsPath, name);
        Files.createDirectories(dir);
        Path file = dir.resolve("SKILL.md");
        Files.writeString(file, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Delete a skill directory from Vita-skills.
     */
    public void deleteSkillFile(String name) throws IOException {
        Path dir = Paths.get(vitaSkillsPath, name);
        if (Files.exists(dir)) {
            try (var s = Files.walk(dir)) {
                s.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
    }

    // ---- Sync: read Vita-skills files and upsert into DB ----

    private static final Pattern FRONTMATTER = Pattern.compile(
            "^---\\s*\\n(.*?)\\n---\\s*\\n(.*)", Pattern.DOTALL);
    private static final Pattern YAML_KEY = Pattern.compile("^(\\w+):\\s*(.+)$", Pattern.MULTILINE);

    /**
     * Scan Vita-skills directory, parse every SKILL.md, and upsert into the database.
     * Returns a summary of what was synced.
     */
    @Transactional
    public Map<String, Object> syncFromVitaSkills() throws IOException {
        Path root = Paths.get(vitaSkillsPath);
        if (!Files.exists(root)) {
            return Map.of("synced", 0, "message", "Vita-skills目录不存在: " + vitaSkillsPath);
        }

        List<String> synced = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (var dirs = Files.list(root)) {
            dirs.filter(Files::isDirectory).forEach(dir -> {
                Path skillFile = dir.resolve("SKILL.md");
                if (!Files.exists(skillFile)) return;
                try {
                    String raw = Files.readString(skillFile);
                    String dirName = dir.getFileName().toString();

                    // Parse YAML frontmatter
                    Matcher fm = FRONTMATTER.matcher(raw);
                    final String name;
                    final String description;
                    final String content;
                    if (fm.matches()) {
                        Map<String, String> fmMap = parseYamlMap(fm.group(1));
                        name = fmMap.getOrDefault("name", dirName);
                        description = fmMap.getOrDefault("description", "");
                        content = fm.group(2).trim();
                    } else {
                        name = dirName;
                        description = "";
                        content = raw;
                    }

                    // Upsert: try find by name
                    List<Skill> existing = skillRepository.findAll();
                    Skill match = existing.stream()
                            .filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(name))
                            .findFirst().orElse(null);

                    if (match != null) {
                        match.setDescription(description);
                        match.setContent(content);
                        match.setCategory(dirName);
                        skillRepository.save(match);
                    } else {
                        Skill s = Skill.builder()
                                .name(name)
                                .description(description)
                                .content(content)
                                .category(dirName)
                                .isActive(true)
                                .status(Skill.Status.APPROVED)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        skillRepository.save(s);
                    }
                    synced.add(name);
                } catch (IOException e) {
                    errors.add(dir.getFileName() + ": " + e.getMessage());
                }
            });
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("synced", synced.size());
        result.put("names", synced);
        result.put("errors", errors);
        return result;
    }

    private Map<String, String> parseYamlMap(String yaml) {
        Map<String, String> map = new LinkedHashMap<>();
        Matcher m = YAML_KEY.matcher(yaml);
        while (m.find()) {
            map.put(m.group(1).trim(), m.group(2).trim());
        }
        return map;
    }

    // ---- System prompt ----

    public String getSystemPrompt() {
        StringBuilder sb = new StringBuilder(MEDICAL_SYSTEM_PROMPT);

        List<Skill> activeSkills = getActiveSkills();
        if (!activeSkills.isEmpty()) {
            sb.append("\n\n【动态加载的专业技能】\n");
            for (Skill skill : activeSkills) {
                sb.append("\n### ").append(skill.getName()).append("\n");
                sb.append(skill.getContent()).append("\n");
            }
        }

        return sb.toString();
    }

    public String getConversationStartPrompt() {
        return """
            您好！我是VitaAI智慧医生助手。我可以帮您：
            - 🩺 分析您的症状并提供初步诊断建议
            - 💊 提供疾病和药品的相关信息
            - 📋 为您生成健康评估报告
            - 🏥 根据严重程度建议是否需要就医

            请描述您的不适症状，我会尽力帮助您。例如：
            - "我头痛3天了，伴有发热和乏力"
            - "最近总是胸闷气短，活动后加重"
            - "我想了解高血压的注意事项"

            【内容为AI诊断，想要更准确诊断，请去正规医院就诊。】
            """;
    }
}
