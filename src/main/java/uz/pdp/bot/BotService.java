package uz.pdp.bot;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import uz.pdp.DB;
import uz.pdp.entity.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BotService {
    public static TelegramBot telegramBot = new TelegramBot("7287868726:AAE7ykkxObrqRWrrSuRmFUO_tNidn1Uvptk");

    public static TgUser getOrCreateUser(Long chatId, String firstName, String lastName) {
        for (TgUser tgUser : DB.USERS) {
            if (tgUser.getChatId().equals(chatId)){
                return tgUser;
            }
        }
        TgUser tgUser = new TgUser();
        tgUser.setChatId(chatId);
        tgUser.setFirstName(firstName);
        if (lastName != null){
            tgUser.setLastName(lastName);
        }
        tgUser.setLastName("");
        DB.USERS.add(tgUser);
        return tgUser;
    }

    public static void acceptStartAndShowDetailsOfUsers(TgUser tgUser) throws IOException, InterruptedException {
        SendMessage sendMessage = new SendMessage(
                tgUser.getChatId(),
                """
                        ✋Assalomu aleykum botimizga xush kelibsiz,
                        hurmatli %s %s👤
                        
                        Davom etish uchun quyidagi tugmalardan birini tanlang
                        👇👇👇
                        """.formatted(tgUser.getFirstName(), tgUser.getLastName())
        );
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Message message = sendResponse.message();

        Integer messageId = message.messageId();

        tgUser.setLastSentMessageId(messageId);
        tgUser.setState(TgState.SHOWING_USERS);
        showingUsersWithPostButtons(tgUser);
    }

    public static void showingUsersWithPostButtons(TgUser tgUser) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String str = response.body();
        Gson gson = new Gson();
        List<User> users = gson.fromJson(str, new TypeToken<List<User>>() {}.getType());
        SendMessage sendMessage = new SendMessage(
                tgUser.getChatId(),
                """
                        USERS:
                        """
        );
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        int countTodo = 0;
        int countPost = 0;
        int countAlbum = 0;
        for (User user : users) {
            countTodo++;
            inlineKeyboardMarkup.addRow(
                    new InlineKeyboardButton("\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB" + user.getName()).callbackData("user"),
                    new InlineKeyboardButton("✏️todos").callbackData("todoss" + countTodo++),
                    new InlineKeyboardButton("\uD83D\uDCE8posts").callbackData("postss" + ++countPost),
                    new InlineKeyboardButton("\uD83D\uDCD3albums").callbackData("albumss" + ++countAlbum)
            );
            sendMessage.replyMarkup(inlineKeyboardMarkup);
        }
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        Message message = sendResponse.message();

        Integer messageId = message.messageId();

        tgUser.setLastSentMessageId(messageId);
        tgUser.setState(TgState.SHOWING_POSTS);
    }

    public static void showingTodosWithPostsAndAlbums(TgUser tgUser, String data) throws IOException, InterruptedException {
        switch (data){
            case "todoss1" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                        if (todo.getUserId().equals(1) && todo.isCompleted()) {
                            inlineKeyboardMarkup.addRow(
                                    new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                    new InlineKeyboardButton("✅").callbackData("tick")
                            );
                        }
                        if (todo.getUserId().equals(1) && !todo.isCompleted()) {
                            inlineKeyboardMarkup.addRow(
                                    new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                    new InlineKeyboardButton("❌").callbackData("x")
                            );
                        }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }
            case "todoss2" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(1) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(1) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss3" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(2) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(2) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss4" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(2) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(2) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss5" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(3) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(3) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss6" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(3) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(3) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss7" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(4) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(4) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss8" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(4) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(4) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss9" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(5) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(5) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss10" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(5) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(5) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss11" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(6) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(6) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss12" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(6) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(6) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss13" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(7) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(7) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss14" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(7) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(7) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss15" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(8) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(8) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss16" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(8) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(8) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss17" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(9) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(9) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss18" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(9) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(9) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss19" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Todo todo : todos) {
                    if (todo.getUserId().equals(10) && todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("✅").callbackData("tick")
                        );
                    }
                    if (todo.getUserId().equals(10) && !todo.isCompleted()) {
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                new InlineKeyboardButton("❌").callbackData("x")
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }case "todoss20" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                TODOS:
                                """
                );

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/todos"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Todo> todos = gson.fromJson(str, new TypeToken<List<Todo>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                todos.stream()
                        .filter(todo -> todo.getUserId().equals(10))
                                .forEach(todo ->
                                {
                                    if (todo.isCompleted()) {
                                        inlineKeyboardMarkup.addRow(
                                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                                new InlineKeyboardButton("✅").callbackData("tick")
                                        );
                                    }else{
                                        inlineKeyboardMarkup.addRow(
                                                new InlineKeyboardButton(todo.getTitle()).callbackData("todo"),
                                                new InlineKeyboardButton("❌").callbackData("x")
                                        );
                                    }
                                });

                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("back")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
            }
            case "postss1" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(1)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + ++countComment)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss2" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(2)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss3" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(3)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss4" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(4)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss5" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(5)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss6" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(6)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss7" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(7)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss8" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(8)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss9" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(9)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }case "postss10" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                POSTS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countComment = 0;
                for (Post post : posts) {
                    if (post.getUserId().equals(10)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(post.getTitle()).callbackData("post1"),
                                new InlineKeyboardButton("\uD83D\uDCACcomments").callbackData("commentary" + countComment++)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_COMMENTS);
            }
            case "albumss1" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(1)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album1"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backPost")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss2" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(2)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album2"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss3" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(3)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album3"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss4" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(4)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album4"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss5" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(5)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album5"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss6" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(6)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album6"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss7" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(7)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album7"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss8" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(8)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album8"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss9" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(9)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album9"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }case "albumss10" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                SendMessage sendMessage = new SendMessage(
                        tgUser.getChatId(),
                        """
                                ALBUMS:
                                """
                );
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/albums"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String str = response.body();
                Gson gson = new Gson();
                List<Album> albums = gson.fromJson(str, new TypeToken<List<Album>>() {}.getType());

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                int countPhoto = 0;
                for (Album album : albums) {
                    if (album.getUserId().equals(10)){
                        inlineKeyboardMarkup.addRow(
                                new InlineKeyboardButton(album.getTitle()).callbackData("album10"),
                                new InlineKeyboardButton("\uD83D\uDCACphotos").callbackData("photo" + ++countPhoto)
                        );
                    }
                }
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("⬅Orqaga").callbackData("backAlbum")
                );
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                SendResponse sendResponse = telegramBot.execute(sendMessage);
                Message message = sendResponse.message();

                Integer messageId = message.messageId();

                tgUser.setLastSentMessageId(messageId);
                tgUser.setState(TgState.VIEWING_PHOTOS);
            }
            case "back" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                showingUsersWithPostButtons(tgUser);
            }
        }
    }

    public static void showingCommentsWithFullDetails(TgUser tgUser, String data) throws IOException, InterruptedException {
        switch (data){
            case "commentary1" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/1/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());

                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(1)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary2" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/2/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());


                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(2)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary3" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/3/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());




                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(3)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary4" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/4/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());




                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(4)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary5" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/5/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());




                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(5)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary6" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/6/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());




                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(6)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary7" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/7/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());




                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(7)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary8" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/8/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());




                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(8)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary9" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/9/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());




                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(9)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "commentary10" -> {
                try {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/10/comments"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String str = response.body();
                    Gson gson = new Gson();
                    List<Comment> comments = gson.fromJson(str, new TypeToken<List<Comment>>() {}.getType());

                    StringBuilder stringBuilder = new StringBuilder();

                    for (Comment comment : comments) {
                            if (comment.getPostId().equals(10)) {
                                stringBuilder.append("email: ").append(comment.getEmail()).append("\n");
                                stringBuilder.append("title: ").append(comment.getName()).append("\n");
                                stringBuilder.append("body: ").append(comment.getBody()).append("\n");
                            }
                    }
                    SendMessage sendMessage = new SendMessage(
                            tgUser.getChatId(),
                            """
                                    COMMENTS
                                    %s
                                    """.formatted(stringBuilder.toString())
                    );
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.addRow(
                         new InlineKeyboardButton("⬅Orqaga").callbackData("backto")
                    );
                    sendMessage.replyMarkup(inlineKeyboardMarkup);
                    telegramBot.execute(sendMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            case "backPost" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                showingUsersWithPostButtons(tgUser);
            }
            case "backto" -> {
                DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                telegramBot.execute(deleteMessage);
                showingUsersWithPostButtons(tgUser);
            }
        }
    }

    public static void showingPhotosWithFullDetails(TgUser tgUser, String data) throws IOException, InterruptedException {
        switch (data) {
            case "photo1" -> {
                if (tgUser != null && tgUser.getChatId() != null && tgUser.getLastSentMessageId() != null) {
                    DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                    telegramBot.execute(deleteMessage);
                }

                HttpClient httpClient = HttpClient.newHttpClient();

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/photos"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String str = response.body();

                        Gson gson = new Gson();
                        List<Photo> photos = gson.fromJson(str, new TypeToken<List<Photo>>() {}.getType());

                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("⬅ Orqaga").callbackData("back"));

                        for (Photo photo : photos) {
                            if (photo.getAlbumId().equals(1)) {

                                HttpRequest imageRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(photo.getUrl()))
                                        .build();
                                HttpResponse<byte[]> imageResponse = httpClient.send(imageRequest, HttpResponse.BodyHandlers.ofByteArray());

                                if (imageResponse.statusCode() == 200) {
                                    byte[] imageBytes = imageResponse.body();

                                    SendPhoto sendPhoto = new SendPhoto(tgUser.getChatId(), imageBytes);
                                    sendPhoto.caption("Title: " + photo.getTitle() + "\nURL: " + photo.getUrl());
                                    sendPhoto.replyMarkup(inlineKeyboardMarkup);

                                    SendResponse sendResponse = telegramBot.execute(sendPhoto);
                                    Message message = sendResponse.message();

                                    if (message != null) {
                                        tgUser.setLastSentMessageId(message.messageId());
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Failed to fetch photos. Status code: " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "photo2" -> {
                if (tgUser != null && tgUser.getChatId() != null && tgUser.getLastSentMessageId() != null) {
                    DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                    telegramBot.execute(deleteMessage);
                }

                HttpClient httpClient = HttpClient.newHttpClient();

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/photos"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String str = response.body();

                        Gson gson = new Gson();
                        List<Photo> photos = gson.fromJson(str, new TypeToken<List<Photo>>() {}.getType());

                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("⬅ Orqaga").callbackData("back"));

                        for (Photo photo : photos) {
                            if (photo.getAlbumId().equals(2)) {

                                HttpRequest imageRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(photo.getUrl()))
                                        .build();
                                HttpResponse<byte[]> imageResponse = httpClient.send(imageRequest, HttpResponse.BodyHandlers.ofByteArray());

                                if (imageResponse.statusCode() == 200) {
                                    byte[] imageBytes = imageResponse.body();

                                    SendPhoto sendPhoto = new SendPhoto(tgUser.getChatId(), imageBytes);
                                    sendPhoto.caption("Title: " + photo.getTitle() + "\nURL: " + photo.getUrl());
                                    sendPhoto.replyMarkup(inlineKeyboardMarkup);

                                    SendResponse sendResponse = telegramBot.execute(sendPhoto);
                                    Message message = sendResponse.message();

                                    if (message != null) {
                                        tgUser.setLastSentMessageId(message.messageId());
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Failed to fetch photos. Status code: " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "photo3" -> {
                if (tgUser != null && tgUser.getChatId() != null && tgUser.getLastSentMessageId() != null) {
                    DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                    telegramBot.execute(deleteMessage);
                }

                HttpClient httpClient = HttpClient.newHttpClient();

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/photos"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String str = response.body();

                        Gson gson = new Gson();
                        List<Photo> photos = gson.fromJson(str, new TypeToken<List<Photo>>() {}.getType());

                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("⬅ Orqaga").callbackData("back"));

                        for (Photo photo : photos) {
                            if (photo.getAlbumId().equals(3)) {

                                HttpRequest imageRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(photo.getUrl()))
                                        .build();
                                HttpResponse<byte[]> imageResponse = httpClient.send(imageRequest, HttpResponse.BodyHandlers.ofByteArray());

                                if (imageResponse.statusCode() == 200) {
                                    byte[] imageBytes = imageResponse.body();

                                    SendPhoto sendPhoto = new SendPhoto(tgUser.getChatId(), imageBytes);
                                    sendPhoto.caption("Title: " + photo.getTitle() + "\nURL: " + photo.getUrl());
                                    sendPhoto.replyMarkup(inlineKeyboardMarkup);

                                    SendResponse sendResponse = telegramBot.execute(sendPhoto);
                                    Message message = sendResponse.message();

                                    if (message != null) {
                                        tgUser.setLastSentMessageId(message.messageId());
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Failed to fetch photos. Status code: " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "photo4" -> {
                if (tgUser != null && tgUser.getChatId() != null && tgUser.getLastSentMessageId() != null) {
                    DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                    telegramBot.execute(deleteMessage);
                }

                HttpClient httpClient = HttpClient.newHttpClient();

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/photos"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String str = response.body();

                        Gson gson = new Gson();
                        List<Photo> photos = gson.fromJson(str, new TypeToken<List<Photo>>() {}.getType());

                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("⬅ Orqaga").callbackData("back"));

                        for (Photo photo : photos) {
                            if (photo.getAlbumId().equals(4)) {

                                HttpRequest imageRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(photo.getUrl()))
                                        .build();
                                HttpResponse<byte[]> imageResponse = httpClient.send(imageRequest, HttpResponse.BodyHandlers.ofByteArray());

                                if (imageResponse.statusCode() == 200) {
                                    byte[] imageBytes = imageResponse.body();

                                    SendPhoto sendPhoto = new SendPhoto(tgUser.getChatId(), imageBytes);
                                    sendPhoto.caption("Title: " + photo.getTitle() + "\nURL: " + photo.getUrl());
                                    sendPhoto.replyMarkup(inlineKeyboardMarkup);

                                    SendResponse sendResponse = telegramBot.execute(sendPhoto);
                                    Message message = sendResponse.message();

                                    if (message != null) {
                                        tgUser.setLastSentMessageId(message.messageId());
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Failed to fetch photos. Status code: " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }case "photo5" -> {
                tgUser.setStopPrinting(false);
                if (tgUser != null && tgUser.getChatId() != null && tgUser.getLastSentMessageId() != null) {
                    DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                    telegramBot.execute(deleteMessage);
                }
                HttpClient httpClient = HttpClient.newHttpClient();
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/photos"))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String str = response.body();

                        Gson gson = new Gson();
                        List<Photo> photos = gson.fromJson(str, new TypeToken<List<Photo>>() {}.getType());

                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("⬅ Orqaga").callbackData("back"));

                        for (Photo photo : photos) {
                            if (tgUser.isStopPrinting()) {
                                return;
                            }
                            if (photo.getAlbumId().equals(5)) {

                                HttpRequest imageRequest = HttpRequest.newBuilder()
                                        .uri(URI.create(photo.getUrl()))
                                        .build();
                                HttpResponse<byte[]> imageResponse = httpClient.send(imageRequest, HttpResponse.BodyHandlers.ofByteArray());

                                if (imageResponse.statusCode() == 200) {
                                    byte[] imageBytes = imageResponse.body();

                                    SendPhoto sendPhoto = new SendPhoto(tgUser.getChatId(), imageBytes);
                                    sendPhoto.caption("Title: " + photo.getTitle() + "\nURL: " + photo.getUrl());
                                    sendPhoto.replyMarkup(inlineKeyboardMarkup);

                                    SendResponse sendResponse = telegramBot.execute(sendPhoto);
                                    Message message = sendResponse.message();

                                    if (message != null) {
                                        tgUser.setLastSentMessageId(message.messageId());
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Failed to fetch photos. Status code: " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            case "back" -> {
                tgUser.setStopPrinting(true);
                showingUsersWithPostButtons(tgUser);
            }
            case "backAlbum" -> {
                      DeleteMessage deleteMessage = new DeleteMessage(tgUser.getChatId(), tgUser.getLastSentMessageId());
                      telegramBot.execute(deleteMessage);
                      showingUsersWithPostButtons(tgUser);
                }
              }
            }
        }