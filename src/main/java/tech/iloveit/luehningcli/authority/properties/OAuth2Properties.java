package tech.iloveit.luehningcli.authority.properties;

import lombok.Data;

import java.util.Map;

@Data
public class OAuth2Properties {
    /**
     * jwtSigningKey
     */
    private String jwtSigningKey = "security";
    /**
     * 确认授权页面
     */
    private String confirmUrl = "/oauth/confirm_access";
    /**
     * token增强信息
     */
    private Map<String, Object> tokenInfo;
    /**
     * 客户端信息
     */
    private OAuth2ClientProperties[] clients = {};

    public Map<String, Object> getTokenInfo() {
        return tokenInfo;
    }

    public void setTokenInfo(Map<String, Object> tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    public OAuth2ClientProperties[] getClients() {
        return clients;
    }

    public void setClients(OAuth2ClientProperties[] clients) {
        this.clients = clients;
    }

    public String getJwtSigningKey() {
        return jwtSigningKey;
    }

    public void setJwtSigningKey(String jwtSigningKey) {
        this.jwtSigningKey = jwtSigningKey;
    }
}
