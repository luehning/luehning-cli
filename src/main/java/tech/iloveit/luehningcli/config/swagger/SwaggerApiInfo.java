package tech.iloveit.luehningcli.config.swagger;

import lombok.Data;

/**
 * @description 配置Swagger Docket 的信息
 */
@Data
public class SwaggerApiInfo {

    private String groupName;

    private String basePackage;

    private String version;

    public SwaggerApiInfo() {
        super();
    }

    public SwaggerApiInfo(String groupName, String basePackage, String version) {
        this.groupName = groupName;
        this.basePackage = basePackage;
        this.version = version;
    }
}
