CREATE DATABASE GuIA;
USE GuIA;
CREATE TABLE empleados (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           nombre VARCHAR(50) NOT NULL,
                           apellido VARCHAR(50) NOT NULL,
                           nombre_empresa VARCHAR(100) NOT NULL,
                           correo_electronico VARCHAR(100) UNIQUE NOT NULL,
                           contraseña VARCHAR(255) NOT NULL,
                           codigo_pais VARCHAR(5) NOT NULL,
                           telefono VARCHAR(20) NOT NULL,
                           imagen_perfil LONGBLOB,
                           fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);