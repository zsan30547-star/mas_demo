// /backend/src/main/java/edu/nslk/imylm/service/impl/AgentServiceImpl.java
// 职责描述：Agent 配置业务实现

package edu.nslk.imylm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.nslk.imylm.entity.AgentConfig;
import edu.nslk.imylm.mapper.AgentConfigMapper;
import edu.nslk.imylm.service.AgentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentServiceImpl extends ServiceImpl<AgentConfigMapper, AgentConfig> implements AgentService {

    @Override
    public List<AgentConfig> getByUserId(Long userId) {
        LambdaQueryWrapper<AgentConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentConfig::getUserId, userId)
               .or()
               .eq(AgentConfig::getIsPreset, 1);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<AgentConfig> getPresets() {
        LambdaQueryWrapper<AgentConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentConfig::getIsPreset, 1);
        return baseMapper.selectList(wrapper);
    }
}
