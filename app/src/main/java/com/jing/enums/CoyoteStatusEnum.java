package com.jing.enums;

/**
 * @Author：静
 * @Package：com.jing.enums
 * @Project：灵静
 * @name：StatusEnum
 * @Date：2024/10/16 下午7:47
 * @Filename：StatusEnum
 * @Version：1.0.0
 */
public enum CoyoteStatusEnum {

    /**
     * 玩具状态枚举
     */
    //已连接
    CONNECTED(1,"郊狼已连接"),
    //未连接
    DISCONNECTED(2,"郊狼未连接"),
    //通信中
    COMMUNICATING(3,"正在运行"),
   ;

    private int status;

    private String msg;

    CoyoteStatusEnum(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static CoyoteStatusEnum getEnumByStatus(int status){
        for(CoyoteStatusEnum statusEnum : CoyoteStatusEnum.values()){
            if(statusEnum.getStatus() == status){
                return statusEnum;
            }
        }
        return null;
    }
}
