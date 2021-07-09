package tech.iloveit.luehningcli.response;

import lombok.Data;

/**
 * @description 统一响应格式
 */
@Data
public class ResponseResult {
    private int code;
    private String msg;
    private Object data;

    public ResponseResult(){}

    public ResponseResult(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResponseResult success(Object data){
        return new ResponseResult(200,"success",data);
    }

    public static ResponseResult success(){
        return new ResponseResult(200,"success","");
    }

    public static ResponseResult info(String msg){
        return new ResponseResult(600,msg,"600");
    }

    public static ResponseResult fail(Object data){
        return new ResponseResult(500,"server_error",data);
    }

    public static ResponseResult fail(){
        return new ResponseResult(500,"server_error","500");
    }

}
