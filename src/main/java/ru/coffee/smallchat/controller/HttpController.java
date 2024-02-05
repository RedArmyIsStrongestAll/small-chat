package ru.coffee.smallchat.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.dto.*;
import ru.coffee.smallchat.service.MainService;

import java.util.List;
import java.util.concurrent.Callable;

@RestController("/chat")
public class HttpController {
    private MainService mainService;

    @Autowired
    public HttpController(MainService mainService) {
        this.mainService = mainService;
    }

    @PostMapping("/sign/in")
    @Operation(summary = "регистрация имени и фотография",
            description = "На сервере spring испльзуется @RequestParam анотация для MultipartFile класс для поля photo")
    public Callable<ResponseDTO<String>> registry(HttpSession httpSession,
                                                  @RequestParam(value = "photo", required = false) MultipartFile photo,
                                                  @RequestParam(value = "name", required = false) String name) {
        return () -> mainService.registry(name, photo, httpSession.getId());
    }

    @GetMapping("/user")
    @Operation(summary = "получение имени и фотографии по uuid")
    public ResponseDTO<UserDTO> getUserByUuid(HttpSession httpSession,
                                              @RequestParam("uuid") String userUuid) {
        return mainService.getUserByUuid(userUuid, httpSession.getId());
    }

    @GetMapping("/public/history")
    @Operation(summary = "поулчение истории общего чата",
            description = "offset начинается с 0")
    public ResponseDTO<List<PublicMessageResponseDTO>> getPublicHistory(HttpSession httpSession,
                                                                        @RequestParam("offset") Integer offset) {
        return mainService.getPublicHistory(httpSession.getId(), offset);
    }

    @GetMapping("/personal/list")
    @Operation(summary = "поулчение спсика личных чатов")
    public ResponseDTO<List<ChatDTO>> getPersonalChatList(HttpSession httpSession) {
        return mainService.getPersonalChatList(httpSession.getId());
    }

    @GetMapping("/personal/history")
    @Operation(summary = "поулчение истории личного чата",
            description = "offset начинается с 0")
    public ResponseDTO<List<PersonalMessageResponseDTO>> getPersonalHistory(HttpSession httpSession,
                                                                            @RequestParam("chatId") Long chatId,
                                                                            @RequestParam("offset") Integer offset) {
        return mainService.getPersonalHistory(chatId, httpSession.getId(), offset);
    }
}
