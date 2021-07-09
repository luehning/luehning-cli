package tech.iloveit.luehningcli.authority.service;

public class DefaultSmsCheck implements tech.iloveit.luehningcli.authority.service.AbstractCheckSmsCode {
    @Override
    public Boolean checkCode(String mobile, String code) {
        System.out.println("手机号： "+mobile +",验证码： "+code+"验证码通过");
        return true;
    }
}
