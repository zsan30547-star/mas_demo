// /backend/src/main/java/edu/nslk/imylm/config/DataInitializer.java
// 职责描述：应用启动时初始化预置模型、Agent、工作流模板和默认管理员

package edu.nslk.imylm.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import edu.nslk.imylm.entity.AgentConfig;
import edu.nslk.imylm.entity.ApiCredential;
import edu.nslk.imylm.entity.ModelConfig;
import edu.nslk.imylm.entity.SysUser;
import edu.nslk.imylm.entity.WorkflowTemplate;
import edu.nslk.imylm.mapper.AgentConfigMapper;
import edu.nslk.imylm.mapper.ApiCredentialMapper;
import edu.nslk.imylm.mapper.ModelConfigMapper;
import edu.nslk.imylm.mapper.UserMapper;
import edu.nslk.imylm.mapper.WorkflowTemplateMapper;
import edu.nslk.imylm.util.AesEncryptUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final ModelConfigMapper modelConfigMapper;
    private final AgentConfigMapper agentConfigMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApiCredentialMapper apiCredentialMapper;

    @Value("${preset.deepseek-api-key:}")
    private String deepseekApiKey;

    public DataInitializer(UserMapper userMapper,
                           ModelConfigMapper modelConfigMapper,
                           AgentConfigMapper agentConfigMapper,
                           WorkflowTemplateMapper workflowTemplateMapper,
                           PasswordEncoder passwordEncoder,
                           ApiCredentialMapper apiCredentialMapper) {
        this.userMapper = userMapper;
        this.modelConfigMapper = modelConfigMapper;
        this.agentConfigMapper = agentConfigMapper;
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.passwordEncoder = passwordEncoder;
        this.apiCredentialMapper = apiCredentialMapper;
    }

    @Override
    public void run(String... args) {
        initAdminUser();
        initPresetModels();
        initPresetAgents();
        initPresetWorkflow();
    }

    private void initAdminUser() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, "admin");
        if (userMapper.selectCount(wrapper) == 0) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setStatus(1);
            userMapper.insert(admin);
        }
    }

    private void initPresetModels() {
        if (modelConfigMapper.selectCount(null) > 0) return;
        LambdaUpdateWrapper<SysUser> w = new LambdaUpdateWrapper<>();
        w.eq(SysUser::getUsername, "admin");
        Long adminId = userMapper.selectOne(w).getId();

        ApiCredential cred = new ApiCredential();
        cred.setUserId(adminId);
        cred.setName("DeepSeek (默认测试凭证)");
        cred.setEndpoint("https://api.deepseek.com/v1");
        String apiKey = deepseekApiKey.isEmpty() ? "sk-d239c687f6f445ffaadae37df3dedbf7" : deepseekApiKey;
        cred.setApiKeyEnc(AesEncryptUtil.encrypt(apiKey));
        apiCredentialMapper.insert(cred);

        createModel(adminId, "DeepSeek", "deepseek-chat", cred.getId());
        createModel(adminId, "Qwen-VL", "qwen-vl-plus", null);
        createModel(adminId, "Gemini", "gemini-2.0-flash", null);
        createModel(adminId, "Claude", "claude-3-5-sonnet", null);
    }

    private void initPresetAgents() {
        if (agentConfigMapper.selectCount(null) > 0) return;
        LambdaUpdateWrapper<SysUser> w = new LambdaUpdateWrapper<>();
        w.eq(SysUser::getUsername, "admin");
        Long adminId = userMapper.selectOne(w).getId();

        LambdaQueryWrapper<ModelConfig> mw = new LambdaQueryWrapper<>();
        mw.eq(ModelConfig::getIsPreset, 1);
        List<ModelConfig> models = modelConfigMapper.selectList(mw);
        Long deepseekId = 0L, qwenId = 0L, geminiId = 0L, claudeId = 0L;
        for (ModelConfig m : models) {
            switch (m.getModel()) {
                case "deepseek-chat": deepseekId = m.getId(); break;
                case "qwen-vl-plus": qwenId = m.getId(); break;
                case "gemini-2.0-flash": geminiId = m.getId(); break;
                case "claude-3-5-sonnet": claudeId = m.getId(); break;
            }
        }

        createAgent(adminId, deepseekId, "规划专家", "planner",
            "你是一个任务规划专家，负责将复杂任务拆解为可执行的子步骤并规划执行顺序。", 0.3, 4096, "brain");
        createAgent(adminId, qwenId, "视觉识别专家", "vision",
            "你是一个视觉分析专家，擅长从图片中提取文字信息、识别物体和场景。", 0.3, 4096, "eye");
        createAgent(adminId, geminiId, "内容执行专家", "executor",
            "你是一个内容执行专家，根据任务规划和参考资料执行具体任务。", 0.7, 8192, "pen");
        createAgent(adminId, claudeId, "质量验证专家", "validator",
            "你是一个质量验证专家，负责检查结果的准确性和逻辑一致性。", 0.3, 4096, "check");
        createAgent(adminId, deepseekId, "信息搜索专家", "search",
            "你是一个搜索专家，根据需求搜索最新信息并整理汇总。", 0.5, 4096, "search");
    }

    private void initPresetWorkflow() {
        if (workflowTemplateMapper.selectCount(null) > 0) return;
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        w.eq(SysUser::getUsername, "admin");
        Long adminId = userMapper.selectOne(w).getId();

        LambdaQueryWrapper<AgentConfig> aw = new LambdaQueryWrapper<>();
        aw.eq(AgentConfig::getIsPreset, 1);
        List<AgentConfig> agents = agentConfigMapper.selectList(aw);
        Long plannerId = 0L, searchId = 0L, executorId = 0L, validatorId = 0L;
        for (AgentConfig a : agents) {
            switch (a.getAgentType()) {
                case "planner": plannerId = a.getId(); break;
                case "search": searchId = a.getId(); break;
                case "executor": executorId = a.getId(); break;
                case "validator": validatorId = a.getId(); break;
            }
        }

        String steps1 = String.format(
            "[{\"agentId\":%d,\"order\":1,\"inputTemplate\":\"将以下课题拆解为研究子任务：$i\"}," +
            "{\"agentId\":%d,\"order\":2,\"inputTemplate\":\"搜索以下主题的最新信息：$o1\"}," +
            "{\"agentId\":%d,\"order\":3,\"inputTemplate\":\"基于以下资料撰写完整研究报告：$o2\"}," +
            "{\"agentId\":%d,\"order\":4,\"inputTemplate\":\"检查以下报告的质量并修正：$o3\"}]",
            plannerId, searchId, executorId, validatorId);

        WorkflowTemplate t1 = new WorkflowTemplate();
        t1.setUserId(adminId);
        t1.setName("研究报告生成");
        t1.setDescription("规划->搜索->撰写->验证，输出完整报告");
        t1.setSteps(steps1);
        t1.setStatus(1);
        t1.setIsPreset(1);
        workflowTemplateMapper.insert(t1);
    }

    private void createModel(Long adminId, String name, String model, Long apiCredentialId) {
        ModelConfig mc = new ModelConfig();
        mc.setUserId(adminId);
        mc.setName(name);
        mc.setModel(model);
        mc.setApiCredentialId(apiCredentialId);
        mc.setIsPreset(1);
        modelConfigMapper.insert(mc);
    }

    private void createAgent(Long adminId, Long modelConfigId, String name, String type,
                             String prompt, double temp, int tokens, String icon) {
        AgentConfig agent = new AgentConfig();
        agent.setUserId(adminId);
        agent.setModelConfigId(modelConfigId);
        agent.setName(name);
        agent.setAgentType(type);
        agent.setSystemPrompt(prompt);
        agent.setTemperature(BigDecimal.valueOf(temp));
        agent.setMaxTokens(tokens);
        agent.setIcon(icon);
        agent.setSortOrder(0);
        agent.setEnabled(1);
        agent.setIsPreset(1);
        agentConfigMapper.insert(agent);
    }
}
