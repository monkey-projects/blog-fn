(ns monkey.blog.servlet
  "Generates the servlet code to handle AppEngine requests"
  (:import [javax.servlet.http HttpServletRequest HttpServletResponse]))

(gen-class :name "monkey.blog.Servlet"
           :prefix "servlet-"
           :extends javax.servlet.http.HttpServlet)

(defn servlet-doGet [_ ^HttpServletRequest req ^HttpServletResponse resp]
  (.. resp (getWriter) (write "Servlet GET invoked\n")))

(defn servlet-doPost [_ ^HttpServletRequest req ^HttpServletResponse resp]
  )

(defn servlet-doPut [_ ^HttpServletRequest req ^HttpServletResponse resp]
  )

(defn servlet-doDelete [_ ^HttpServletRequest req ^HttpServletResponse resp]
  )
