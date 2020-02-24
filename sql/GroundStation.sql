-- MySQL dump 10.13  Distrib 8.0.15, for macos10.14 (x86_64)
--
-- Host: localhost    Database: GroundStation
-- ------------------------------------------------------
-- Server version	8.0.15

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Administrators`
--

DROP TABLE IF EXISTS `Administrators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `Administrators` (
  `AdminID` int(11) NOT NULL AUTO_INCREMENT,
  `classcode` varchar(100) NOT NULL,
  `userID` int(11) DEFAULT NULL,
  PRIMARY KEY (`AdminID`),
  KEY `fk1_idx` (`userID`),
  CONSTRAINT `fk1` FOREIGN KEY (`userID`) REFERENCES `userinfo` (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Administrators`
--

LOCK TABLES `Administrators` WRITE;
/*!40000 ALTER TABLE `Administrators` DISABLE KEYS */;
/*!40000 ALTER TABLE `Administrators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Scheduling`
--

DROP TABLE IF EXISTS `Scheduling`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `Scheduling` (
  `schedID` int(11) NOT NULL,
  `dated` varchar(45) NOT NULL,
  `8` int(11) DEFAULT NULL,
  `9` int(11) DEFAULT NULL,
  `10` int(11) DEFAULT NULL,
  `11` int(11) DEFAULT NULL,
  `12` int(11) DEFAULT NULL,
  `13` int(11) DEFAULT NULL,
  `14` int(11) DEFAULT NULL,
  `15` int(11) DEFAULT NULL,
  `16` int(11) DEFAULT NULL,
  `17` int(11) DEFAULT NULL,
  `18` int(11) DEFAULT NULL,
  `19` int(11) DEFAULT NULL,
  `20` int(11) DEFAULT NULL,
  PRIMARY KEY (`schedID`),
  KEY `8user_idx` (`8`),
  KEY `9user_idx` (`9`),
  KEY `10user_idx` (`10`),
  KEY `11user_idx` (`11`),
  KEY `12user_idx` (`12`),
  KEY `13user_idx` (`13`),
  KEY `14user_idx` (`15`),
  KEY `16user_idx` (`16`),
  KEY `17user_idx` (`17`),
  KEY `18user_idx` (`18`),
  KEY `19user_idx` (`19`),
  KEY `20user_idx` (`20`),
  CONSTRAINT `10user` FOREIGN KEY (`10`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `11user` FOREIGN KEY (`11`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `12user` FOREIGN KEY (`12`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `13user` FOREIGN KEY (`13`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `14user` FOREIGN KEY (`15`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `15user` FOREIGN KEY (`15`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `16user` FOREIGN KEY (`16`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `17user` FOREIGN KEY (`17`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `18user` FOREIGN KEY (`18`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `19user` FOREIGN KEY (`19`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `20user` FOREIGN KEY (`20`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `8user` FOREIGN KEY (`8`) REFERENCES `userinfo` (`userID`),
  CONSTRAINT `9user` FOREIGN KEY (`9`) REFERENCES `userinfo` (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Scheduling`
--

LOCK TABLES `Scheduling` WRITE;
/*!40000 ALTER TABLE `Scheduling` DISABLE KEYS */;
/*!40000 ALTER TABLE `Scheduling` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserInfo`
--

DROP TABLE IF EXISTS `UserInfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `UserInfo` (
  `userID` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `pw` varchar(100) NOT NULL,
  `salt` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`userID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserInfo`
--

LOCK TABLES `UserInfo` WRITE;
/*!40000 ALTER TABLE `UserInfo` DISABLE KEYS */;
INSERT INTO `UserInfo` VALUES (1,'memcclun','pw','salt','Mackenzie');
/*!40000 ALTER TABLE `UserInfo` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-02-23 18:03:49
