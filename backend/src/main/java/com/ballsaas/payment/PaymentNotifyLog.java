package com.ballsaas.payment;

import com.ballsaas.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_notify_log")
public class PaymentNotifyLog extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(nullable = false, length = 40)
    private String notifyType;

    @Column(length = 80)
    private String outTradeNo;

    @Column(length = 120)
    private String transactionId;

    @Column(columnDefinition = "text")
    private String rawBody;

    @Column(columnDefinition = "text")
    private String headers;

    @Column(nullable = false, length = 32)
    private String processStatus;

    @Column(length = 500)
    private String errorMessage;

    protected PaymentNotifyLog() {
    }

    public PaymentNotifyLog(String channel, String notifyType, String outTradeNo, String rawBody, String headers, String processStatus) {
        this.channel = channel;
        this.notifyType = notifyType;
        this.outTradeNo = outTradeNo;
        this.rawBody = rawBody;
        this.headers = headers;
        this.processStatus = processStatus;
    }

    public String getChannel() {
        return channel;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getProcessStatus() {
        return processStatus;
    }
}
