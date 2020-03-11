package moe.ofs.backend.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import moe.ofs.backend.domain.Level;
import moe.ofs.backend.request.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConnectionManager {

    private static final Gson gson = new Gson();

    private static RequestHandler requestHandler = RequestHandler.getInstance();

    public static void sanitizeDataPipeline() {
        new FillerRequest(Level.SERVER).send();
        new FillerRequest(Level.SERVER_POLL).send();
        new FillerRequest(Level.EXPORT).send();
        new FillerRequest(Level.EXPORT_POLL).send();

        RequestHandler.getInstance().transmitAndReceive();
    }

    public static String fastPack(BaseRequest request) {
        List<JsonRpcRequest> container = new ArrayList<>();
        container.add(request.toJsonRpcCall());
        return gson.toJson(container);
    }

    public static boolean fastPackThenSendAndCheck(BaseRequest request) {
        try {
            return requestHandler.sendAndGet(request.getPort(), fastPack(request)) != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String fastPackThenSendAndGet(BaseRequest request) throws IOException {
        return requestHandler.sendAndGet(request.getPort(), fastPack(request));
    }

    public static <T> List<T> flattenResponse(List<JsonRpcResponse<List<T>>> jsonRpcResponseList) {
        return jsonRpcResponseList.stream()
                .flatMap(r -> r.getResult().getData().stream()).collect(Collectors.toList());
    }

    public static <T, R> List<R> flattenResponse(List<JsonRpcResponse<List<T>>> jsonRpcResponseList,
                                                                    Function<T, R> mappingFunction) {
        return jsonRpcResponseList.stream()
                .flatMap(r -> r.getResult().getData().stream())
                .map(mappingFunction)
                .collect(Collectors.toList());
    }

    public static <T> List<JsonRpcResponse<T>> parseJsonResponseToRaw(String jsonString, Class<T> targetClass) {
        // TODO --> this is so sad
        Type jsonRpcResponseListType = TypeToken.getParameterized(List.class,
                TypeToken.getParameterized(JsonRpcResponse.class, targetClass).getType()).getType();

        // TODO -> why? java.lang.IllegalStateException: Expected a string but was BEGIN_ARRAY at line 1 column 81 path $[0].result.data
        return gson.fromJson(jsonString, jsonRpcResponseListType);
    }

    public static <T> List<JsonRpcResponse<List<T>>> parseJsonResponse(String jsonString, Class<T> targetClass) {

        // TODO --> this is so sad
        Type jsonRpcResponseListType = TypeToken.getParameterized(List.class,
                TypeToken.getParameterized(JsonRpcResponse.class,
                        TypeToken.getParameterized(List.class, targetClass).getType()).getType()).getType();

        return gson.fromJson(jsonString, jsonRpcResponseListType);

//        return list.stream()
//                .flatMap(r -> r.getResult().getData().stream())
//                .map(targetClass::cast)
//                .collect(Collectors.toList());
//
//        return gson.fromJson(jsonString, jsonRpcResponseListType);
    }
}
