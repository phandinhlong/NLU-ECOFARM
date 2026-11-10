package jakarta.ecofarm_backend.config;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "SwaggerUiServlet", urlPatterns = {"/doc-api","/swagger-ui"})
public class SwaggerUIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String base = req.getContextPath();
        resp.getWriter().write("""
            <!doctype html>
            <html>
              <head>
                <title>NLU-ECOFARM API</title>
                <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css"/>
              </head>
              <body>
                <div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                <script>
                  SwaggerUIBundle({ url: "%s/openapi.json", dom_id: "#swagger-ui" });
                </script>
              </body>
            </html>
            """.formatted(base));
    }
}