package edu.nslk.imylm.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.nslk.imylm.entity.ApiCredential;
import edu.nslk.imylm.mapper.ApiCredentialMapper;
import edu.nslk.imylm.service.ApiCredentialService;
import org.springframework.stereotype.Service;

@Service
public class ApiCredentialServiceImpl extends ServiceImpl<ApiCredentialMapper, ApiCredential> implements ApiCredentialService {}
