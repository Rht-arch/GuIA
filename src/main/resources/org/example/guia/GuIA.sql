-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 13-06-2025 a las 18:30:10
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `guia`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `empleados`
--

CREATE TABLE `empleados` (
  `id` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `apellido` varchar(50) NOT NULL,
  `nombre_empresa` varchar(100) NOT NULL,
  `correo_electronico` varchar(100) NOT NULL,
  `contraseña` varchar(255) NOT NULL,
  `codigo_pais` varchar(5) NOT NULL,
  `telefono` varchar(20) NOT NULL,
  `imagen_perfil` longblob DEFAULT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `empleados`
--

INSERT INTO `empleados` (`id`, `nombre`, `apellido`, `nombre_empresa`, `correo_electronico`, `contraseña`, `codigo_pais`, `telefono`, `imagen_perfil`, `fecha_registro`) VALUES
(1, 'Rafael', 'haro', 'Prueba', 'rafaharotello2003@gmail.com', '$2a$10$pOBlRWWd5CqjcRDZcx7jj.LU/ef6zdC6dbvrCyCjC7Ya8MdG4coq6', '+34', '658220529', NULL, '2025-05-28 23:18:38'),
(2, 'Prueba', '2', 'pruebaAdmin', 'pruebaAdmin@prueba.com', '$2a$10$awsU2YceWv/8n3lCiMMXmOVAOOyaf.0KaUA.uQ4v9B3pHL4QEhmBe', '+34', '658200236', NULL, '2025-06-02 01:00:48');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `empleados_pin`
--

CREATE TABLE `empleados_pin` (
  `id_empleado` int(11) NOT NULL,
  `pin` varchar(255) NOT NULL,
  `fecha_actualizacion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `empleados_pin`
--

INSERT INTO `empleados_pin` (`id_empleado`, `pin`, `fecha_actualizacion`) VALUES
(1, '$2a$10$QOsr5gtnfc5N0J0cVXGUCeHX2u5sHwg.JhHd4.m4iTPeKajn3NIQW', '2025-06-13 01:01:43');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `eventos`
--

CREATE TABLE `eventos` (
  `id_evento` int(11) NOT NULL,
  `id_empleado` int(11) NOT NULL,
  `fecha_evento` date NOT NULL,
  `descripcion` text NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `eventos`
--

INSERT INTO `eventos` (`id_evento`, `id_empleado`, `fecha_evento`, `descripcion`, `fecha_creacion`) VALUES
(1, 1, '2025-06-09', 'Prueba', '2025-06-09 00:01:55'),
(2, 1, '2025-06-10', 'Prueba2', '2025-06-12 21:25:32');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `fichajes`
--

CREATE TABLE `fichajes` (
  `id_fichaje` int(11) NOT NULL,
  `id_empleado` int(11) NOT NULL,
  `pin` varchar(4) NOT NULL DEFAULT '0000',
  `tipo` enum('entrada','salida') NOT NULL,
  `fecha_hora` timestamp NOT NULL DEFAULT current_timestamp(),
  `pin_usado` varchar(6) NOT NULL DEFAULT '000000',
  `es_pin_valido` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `fichajes`
--

INSERT INTO `fichajes` (`id_fichaje`, `id_empleado`, `pin`, `tipo`, `fecha_hora`, `pin_usado`, `es_pin_valido`) VALUES
(3, 1, '1234', 'entrada', '2025-06-04 00:29:59', '000000', 0),
(4, 1, '1235', 'salida', '2025-06-04 00:30:04', '000000', 0),
(5, 1, '0000', 'entrada', '2025-06-05 23:43:20', '1234', 0),
(6, 1, '0000', 'entrada', '2025-06-05 23:43:24', '1235', 1),
(7, 1, '0000', 'salida', '2025-06-05 23:43:29', '1234', 0),
(8, 1, '0000', 'entrada', '2025-06-05 23:43:32', '1235', 1),
(9, 1, '0000', 'entrada', '2025-06-05 23:43:57', '1235', 0),
(10, 1, '0000', 'entrada', '2025-06-05 23:44:00', '1256', 1),
(11, 1, '0000', 'salida', '2025-06-05 23:44:05', '1256', 1),
(12, 1, '0000', 'entrada', '2025-06-12 21:23:42', '1357', 0),
(13, 1, '0000', 'entrada', '2025-06-12 21:24:39', '1357', 0),
(14, 1, '0000', 'entrada', '2025-06-12 21:24:48', '1256', 1),
(15, 1, '0000', 'salida', '2025-06-12 21:24:57', '1256', 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `empleados`
--
ALTER TABLE `empleados`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `correo_electronico` (`correo_electronico`);

--
-- Indices de la tabla `empleados_pin`
--
ALTER TABLE `empleados_pin`
  ADD PRIMARY KEY (`id_empleado`);

--
-- Indices de la tabla `eventos`
--
ALTER TABLE `eventos`
  ADD PRIMARY KEY (`id_evento`),
  ADD KEY `idx_id_empleado_fecha` (`id_empleado`,`fecha_evento`);

--
-- Indices de la tabla `fichajes`
--
ALTER TABLE `fichajes`
  ADD PRIMARY KEY (`id_fichaje`),
  ADD KEY `id_empleado` (`id_empleado`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `empleados`
--
ALTER TABLE `empleados`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `eventos`
--
ALTER TABLE `eventos`
  MODIFY `id_evento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `fichajes`
--
ALTER TABLE `fichajes`
  MODIFY `id_fichaje` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `empleados_pin`
--
ALTER TABLE `empleados_pin`
  ADD CONSTRAINT `empleados_pin_ibfk_1` FOREIGN KEY (`id_empleado`) REFERENCES `empleados` (`id`);

--
-- Filtros para la tabla `eventos`
--
ALTER TABLE `eventos`
  ADD CONSTRAINT `eventos_ibfk_1` FOREIGN KEY (`id_empleado`) REFERENCES `empleados` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `fichajes`
--
ALTER TABLE `fichajes`
  ADD CONSTRAINT `fichajes_ibfk_1` FOREIGN KEY (`id_empleado`) REFERENCES `empleados` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
