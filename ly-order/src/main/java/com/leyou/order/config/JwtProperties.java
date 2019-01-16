package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    // 公钥路径
    private String pubKeyPath;
    // cookie名称
    private String cookieName;

    // 公钥
    private PublicKey publicKey;

    // 应该在spring完成对象初始化后,再去加载公钥和私钥
    @PostConstruct
    public void init() throws Exception{
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}
