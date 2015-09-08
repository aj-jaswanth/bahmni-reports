package org.bahmni.reports.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
public class TemplateUploadController {


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadTemplateFile(@RequestParam(value = "file") MultipartFile file) throws IOException {
        String pathname = "/var/www/report_templates/" + file.getOriginalFilename();
        file.transferTo(new File(pathname));
        return pathname;

    }
}
