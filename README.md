This is the source code for web-based archive of my eBooks and audiobooks.

The assets are stored in an S3-compatible object store hosted by Linode and the asset metadata
is stored in a MariaDB instance running on the web server (also hosed by Linode).  

The implementation is in Java, using Spring Boot and other Spring technologies like Spring Security and jdbctemplate.

