package com.linguacraft.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务:发送重置密码邮件。
 *
 * <p>降级策略:SMTP 未配置(无 JavaMailSender Bean)或发送失败时,
 * 不阻断业务流程,改为在后端日志输出重置链接(便于本地联调)。
 * 配置 SMTP 后(spring.mail.*)自动改为真实发信。</p>
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    /** 邮件发送器(可空:未配置 spring.mail.host 时不存在该 Bean) */
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * 发送重置密码邮件;无发送通道时降级为日志输出链接。
     *
     * @param email     收件邮箱
     * @param resetLink 重置链接(前端 /reset-password?token=xxx)
     */
    public void sendResetMail(String email, String resetLink) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("SMTP 未配置,重置密码链接(开发模式日志输出,30 分钟内有效): {}", resetLink);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("LinguaCraft 密码重置");
            message.setText("你好,\n\n请点击以下链接重置密码(30 分钟内有效):\n" + resetLink
                    + "\n\n如非本人操作,请忽略本邮件。");
            mailSender.send(message);
            log.info("重置密码邮件已发送至: {}", email);
        } catch (Exception e) {
            log.error("重置邮件发送失败,降级为日志输出: {}", resetLink, e);
        }
    }
}