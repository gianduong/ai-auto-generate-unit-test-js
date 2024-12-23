package org;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsFileReader {

    private static final List<String> IGNORED_FOLDERS = Arrays.asList("node_modules", "vendor");

    public void run() {
        String projectDir = System.getProperty("user.dir") + "/src";
//        String projectDir = "C:\\xampp\\htdocs/laravel-facebook-multi-pixels/public/client";
        System.out.println("Working directory: " + projectDir);

        // Tìm tất cả các file .js trong toàn bộ project
        List<Path> jsFiles = findJsFiles(Paths.get(projectDir));

        for (Path jsFile : jsFiles) {
            System.out.println("Processing file: " + jsFile);
            String jsCode = readFile(jsFile.toString());

            if (jsCode.isEmpty()) {
                System.err.println("File " + jsFile + " không có nội dung hoặc không đọc được.");
                continue;
            }

            // Tạo unit test cho file
            String unitTestCode = generateUnitTests(jsCode);

            // Ghi unit test vào file .test.js
            String testFilePath = jsFile.toString().replace(".js", ".test.js");
            ensureDirectoryExists(testFilePath);
            writeFile(testFilePath, removeCodeBlockTags(unitTestCode));
        }
    }

    // Tìm tất cả các file .js trong thư mục
    public List<Path> findJsFiles(Path rootDir) {
        List<Path> jsFiles = new ArrayList<>();
        try {
            Files.walk(rootDir)
                    .filter(path -> {
                        // Bỏ qua các thư mục không mong muốn
                        if (Files.isDirectory(path)) {
                            String folderName = path.getFileName().toString();
                            return !IGNORED_FOLDERS.contains(folderName);
                        }
                        return true;
                    })
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".js") && path.getFileName().toString().contains("func-"))
                    .forEach(jsFiles::add);
        } catch (IOException e) {
            System.err.println("Lỗi khi tìm file .js: " + e.getMessage());
            e.printStackTrace();
        }
        return jsFiles;
    }

    // Hàm đảm bảo thư mục tồn tại
    public void ensureDirectoryExists(String filePath) {
        Path parentDir = Paths.get(filePath).getParent();
        try {
            if (Files.notExists(parentDir)) {
                Files.createDirectories(parentDir);
                System.out.println("Đã tạo thư mục: " + parentDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo thư mục: " + parentDir);
        }
    }

    // Hàm đọc nội dung file vào một chuỗi
    public String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
            e.printStackTrace();
        }
        return content.toString();
    }

    // Hàm gọi API để tạo unit test từ mã JavaScript
    public String generateUnitTests(String jsCode) {
        String apiKey = "KEY"; // Thay bằng API key của bạn
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String textToSend = "viết unit test chi tiết cho các hàm duới đây, chỉ trả code, không giải thích gì cả, chỉ chứa code, đây là hàm:\n" + jsCode;
            String jsonInputString = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", textToSend);

            // Gửi yêu cầu
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Đọc phản hồi từ API
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            System.out.println("API Response: " + response.toString());

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    // Hàm xóa các tag code block như ```javascript và ```
    public String removeCodeBlockTags(String content) {
        return content.replaceAll("```javascript", "").replaceAll("```", "");
    }

    // Hàm ghi nội dung vào một file
    public void writeFile(String filePath, String content) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write(content);
            System.out.println("Unit test đã được ghi vào file: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}