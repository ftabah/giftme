package com.ftabah.giftme.adapter.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the public password-reset page. */
@Controller
public class PasswordResetPageController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetPageController.class);

    @GetMapping(value = "/reset-password", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Resource resetPasswordPage() {
        log.info("Página de redefinição de senha solicitada");
        return new ClassPathResource("static/index.html");
    }
}