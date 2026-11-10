package jakarta.ecofarm_backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "OpenApiServlet", urlPatterns = {"/openapi.json", "/openapi"})
public class OpenApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Sinh OpenAPI spec động dựa trên các resource @Path hiện tại trong code
        String json = OpenApiGenerator.generateJson(req.getContextPath());
        resp.getWriter().write(json);
    }
}

