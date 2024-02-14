package ru.coffee.smallchat.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.dto.*;
import ru.coffee.smallchat.service.MainService;

import java.util.List;

@RestController()
@RequestMapping("/chat")
public class MainHttpController {
    private MainService mainService;

    @Autowired
    public MainHttpController(MainService mainService) {
        this.mainService = mainService;
    }

    private Object getUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/editProfile")
    @Operation(summary = "регистрация имени и фотография",
            description = "На сервере spring испльзуется @RequestParam анотация для MultipartFile класс для поля photo")
    public ResponseDTO<Void> editProfile(@RequestParam(value = "photo", required = false) MultipartFile photo,
                                         @RequestParam(value = "name", required = false) String name) {
        return mainService.editProfile(name, photo, getUserId().toString());
        //todo в swagger комментарий что тут void вернется ResponseDTO<Void>
        //todo + что есть необяхательные вроде не отмечается в swagger
    }

    @GetMapping("/user")
    @Operation(summary = "получение имени и фотографии по id")
    public ResponseDTO<UserDTO> getUserById(@RequestParam("userId") String userId) {
        return mainService.getUserById(userId, getUserId().toString());
    }

    @GetMapping("/public/history")
    @Operation(summary = "поулчение истории общего чата",
            description = "offset начинается с 0")
    public ResponseDTO<List<PublicMessageResponseDTO>> getPublicHistory(@RequestParam("offset") Integer offset) {
        return mainService.getPublicHistory(getUserId().toString(), offset);
    }

    @GetMapping("/personal/list")
    @Operation(summary = "поулчение спсика личных чатов")
    public ResponseDTO<List<ChatDTO>> getPersonalChatList() {
        return mainService.getPersonalChatList(getUserId().toString());
    }

    @GetMapping("/personal/history")
    @Operation(summary = "поулчение истории личного чата",
            description = "offset начинается с 0")
    public ResponseDTO<List<PersonalMessageResponseDTO>> getPersonalHistory(@RequestParam("chatId") Long chatId,
                                                                            @RequestParam("offset") Integer offset) {
        return mainService.getPersonalHistory(chatId, getUserId().toString(), offset);
    }
}
