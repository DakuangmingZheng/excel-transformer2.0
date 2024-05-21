package com.sun.mall.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.mall.entity.InExcel;
import com.sun.mall.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author sunql
 * @description controller层
 * @date 2020/12/29 15:30
 */
@Controller
@RequestMapping(value = "/excel")
public class TestController {
    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile() {
        Resource resource = resourceLoader.getResource("classpath:download.txt");
        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/index")
    public String test() {
        return "index";
    }

    @PostMapping("/importExcel")
    public void importUser(@RequestPart("file") MultipartFile file, HttpServletResponse response) throws Exception {
        List<InExcel> inExcelList = ExcelUtils.readMultipartFile(file, InExcel.class);
        List<Object> head = Arrays.asList("機能","手順","条件","テスト項目・要求ID（内容は要求仕様書参照）","組み合わせNO","合否","実施日");

        List<List<Object>> sheetDataList = new ArrayList<>();
        sheetDataList.add(head);
        for (InExcel inExcel : inExcelList) {
            List<Object> excelList = new ArrayList<>();
            excelList.add("アプリ側で録画機能を開始します。");
            excelList.add(inExcel.getStep());
            excelList.add(inExcel.getConditions());
            excelList.add(inExcel.getAim());
            excelList.add(inExcel.getNo());
            excelList.add("○");
            excelList.add("2023/9/12");
            sheetDataList.add(excelList);
        }
        ExcelUtils.export(response,"export",sheetDataList);
    }
    @PostMapping("/countDiff")
    public void countDiff(@RequestPart("file") MultipartFile multipartFile, HttpServletResponse response) throws Exception {

        //文件上传前的名称
        String fileName = multipartFile.getOriginalFilename();
        assert fileName != null;
        File file = new File(fileName);
        OutputStream out = null;
        try{
            //获取文件流，以文件流的方式输出到新文件
            out = new FileOutputStream(file);
            byte[] ss = multipartFile.getBytes();
            for(int i = 0; i < ss.length; i++){
                out.write(ss[i]);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        BufferedReader r = new BufferedReader(new FileReader(file));
        int count = 0;
        String line = null;
        while ((  line = r.readLine()) != null) {
            if (line.startsWith("+") && !line.startsWith("+++") && !isBlank(line.substring(1))) {
                count++;
            }
        }
        System.out.println("total diff count is " + count);
        r.close();
    }
    @PostMapping("/combination")
    public void combination(@RequestPart("file") MultipartFile file, HttpServletResponse response) throws Exception {
        List<List<String>> fList = new ArrayList<>();
        List<List<Object>> resultList;
        List<String> baseList = new ArrayList<>();
        Map<Integer, List<String>> map = ExcelUtils.getExcelMap(file);
        for(Map.Entry<Integer, List<String>> entry:map.entrySet()){
            if(entry.getKey() == 0){
                baseList = entry.getValue();
            }else{
                fList.add(entry.getValue());
            }
        }
        List<String> finalBaseList = baseList;
        Stream<String> tempStream = finalBaseList.stream();
        for (int i = 0; i < fList.size(); i++) {
            List<String> each = fList.get(i);
            tempStream =tempStream.flatMap(rEach -> each.stream().map(secStr->{
                return rEach + "/" + secStr;
            }));
        }
        baseList =  tempStream.collect(Collectors.toList());
        resultList= baseList.stream().map(s->{
            List<Object> temp=convert(Arrays.stream(s.split("/")).toList()) ;
            return   temp;
        }).collect(Collectors.toList());
        ExcelUtils.export(response,"导出表",resultList);
    }
    private static List<Object> convert(List list) {
        return list;
    }
}