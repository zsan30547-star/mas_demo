// /backend/src/main/java/edu/nslk/imylm/service/impl/ModelConfigServiceImpl.java
// 职责描述：模型配置业务实现

package edu.nslk.imylm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.nslk.imylm.entity.ModelConfig;
import edu.nslk.imylm.mapper.ModelConfigMapper;
import edu.nslk.imylm.service.ModelConfigService;
import org.springframework.stereotype.Service;

@Service
public class ModelConfigServiceImpl extends ServiceImpl<ModelConfigMapper, ModelConfig> implements ModelConfigService {
}
