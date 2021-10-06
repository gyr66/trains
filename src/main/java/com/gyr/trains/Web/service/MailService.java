package com.gyr.trains.Web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MailService {
    @Autowired
    JavaMailSender javaMailSender;

    public void send(String msg) {
        SimpleMailMessage message = new SimpleMailMessage();
        // 设置邮件主题
        message.setSubject("爬虫通知");
        message.setFrom("gyr6792021@163.com");
        message.setTo("157679566@qq.com");
        message.setSentDate(new Date());
        message.setText(msg);
        javaMailSender.send(message);
    }
}
