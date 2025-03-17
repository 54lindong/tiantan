package com.tiantan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiantan.model.data.ScenicSpot;
import com.tiantan.model.data.SpotList;
import com.tiantan.model.graph.EdgeType;
import com.tiantan.model.graph.ScenicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 文件操作工具类
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DATA_DIR = "data";
    
    /**
     * 初始化应用数据目录
     */
    public static void initDataDirectory() {
        Path dataPath = Paths.get(DATA_DIR);
        if (!Files.exists(dataPath)) {
            try {
                Files.createDirectories(dataPath);
                logger.info("创建数据目录: {}", dataPath.toAbsolutePath());
            } catch (IOException e) {
                logger.error("创建数据目录失败", e);
            }
        }
    }
    
    /**
     * 加载景点数据
     * @return 景点列表
     */
    public static SpotList loadScenicSpots() {
        SpotList spotList = new SpotList();
        Path filePath = Paths.get(DATA_DIR, "spots.json");
        
        if (!Files.exists(filePath)) {
            // 如果文件不存在，创建示例数据
            createSampleSpotData();
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            ScenicSpot[] spots = objectMapper.readValue(reader, ScenicSpot[].class);
            for (ScenicSpot spot : spots) {
                spotList.add(spot);
            }
            logger.info("成功加载{}个景点数据", spotList.size());
        } catch (IOException e) {
            logger.error("加载景点数据失败", e);
        }
        
        return spotList;
    }
    
    /**
     * 保存景点数据
     * @param spotList 景点列表
     * @return 是否保存成功
     */
    public static boolean saveScenicSpots(SpotList spotList) {
        Path filePath = Paths.get(DATA_DIR, "spots.json");
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, spotList.toArray());
            logger.info("成功保存{}个景点数据", spotList.size());
            return true;
        } catch (IOException e) {
            logger.error("保存景点数据失败", e);
            return false;
        }
    }
    
    /**
     * 加载景区图数据
     * @param graph 景区图对象
     * @param spotList 景点列表
     * @return 是否加载成功
     */
    public static boolean loadScenicGraph(ScenicGraph graph, SpotList spotList) {
        Path filePath = Paths.get(DATA_DIR, "graph.csv");
        
        if (!Files.exists(filePath)) {
            // 如果文件不存在，创建示例数据
            createSampleGraphData(spotList);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            // 跳过标题行
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    int fromId = Integer.parseInt(parts[0]);
                    int toId = Integer.parseInt(parts[1]);
                    double weight = Double.parseDouble(parts[2]);
                    String type = parts[3];
                    boolean isCrowded = Boolean.parseBoolean(parts[4]);
                    
                    // 添加顶点（如果不存在）
                    for (int i = 0; i < spotList.size(); i++) {
                        ScenicSpot spot = spotList.get(i);
                        graph.addVertex(spot);
                    }
                    
                    // 添加边
                    if (graph.addEdge(fromId, toId, weight, EdgeType.valueOf(type))) {
                        // 设置拥挤状态
                        graph.getVertex(fromId).getEdgeTo(toId).setCrowded(isCrowded);
                    }
                }
            }
            
            logger.info("成功加载景区图数据, 顶点数: {}, 边数: {}", graph.getVertexCount(), graph.getEdgeCount());
            return true;
        } catch (IOException e) {
            logger.error("加载景区图数据失败", e);
            return false;
        }
    }
    
    /**
     * 保存景区图数据
     * @param graph 景区图对象
     * @return 是否保存成功
     */
    public static boolean saveScenicGraph(ScenicGraph graph) {
        Path filePath = Paths.get(DATA_DIR, "graph.csv");
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            // 写入标题行
            writer.write("FromId,ToId,Weight,Type,IsCrowded\n");
            
            // 写入边数据
            for (com.tiantan.model.graph.Edge edge : graph.getEdges()) {
                int fromId = edge.getFrom().getSpot().getId();
                int toId = edge.getTo().getSpot().getId();
                double weight = edge.getWeight();
                String type = edge.getType().name();
                boolean isCrowded = edge.isCrowded();
                
                writer.write(String.format("%d,%d,%.2f,%s,%b\n", 
                                         fromId, toId, weight, type, isCrowded));
            }
            
            logger.info("成功保存景区图数据, 顶点数: {}, 边数: {}", graph.getVertexCount(), graph.getEdgeCount());
            return true;
        } catch (IOException e) {
            logger.error("保存景区图数据失败", e);
            return false;
        }
    }
    
    /**
     * 加载应用程序设置
     * @return 属性对象
     */
    public static Properties loadSettings() {
        Properties properties = new Properties();
        Path filePath = Paths.get(DATA_DIR, "settings.properties");
        
        if (!Files.exists(filePath)) {
            // 如果文件不存在，创建默认设置
            createDefaultSettings();
        }
        
        try (InputStream input = Files.newInputStream(filePath)) {
            properties.load(input);
            logger.info("成功加载应用设置");
        } catch (IOException e) {
            logger.error("加载应用设置失败", e);
        }
        
        return properties;
    }
    
    /**
     * 保存应用程序设置
     * @param properties 属性对象
     * @return 是否保存成功
     */
    public static boolean saveSettings(Properties properties) {
        Path filePath = Paths.get(DATA_DIR, "settings.properties");
        
        try (OutputStream output = Files.newOutputStream(filePath)) {
            properties.store(output, "TianTan Guide Settings");
            logger.info("成功保存应用设置");
            return true;
        } catch (IOException e) {
            logger.error("保存应用设置失败", e);
            return false;
        }
    }
    
    /**
     * 创建示例景点数据
     */
    private static void createSampleSpotData() {
        Path filePath = Paths.get(DATA_DIR, "spots.json");
        
        List<ScenicSpot> sampleSpots = new ArrayList<>();
        
        // 添加天坛景区的主要景点
        sampleSpots.add(new ScenicSpot(1, "祈年殿", "Hall of Prayer for Good Harvests", 
            "祈年殿是天坛的主体建筑，是明清两代帝王祭祀皇天、祈求丰年的场所。建筑呈圆形，象征天，直径32.72米，高38米，全部用木结构建成，不用一钉一铆。", 
            "The Hall of Prayer for Good Harvests is the main building of the Temple of Heaven. It was where emperors of the Ming and Qing dynasties worshipped heaven and prayed for good harvests. The building is circular, symbolizing heaven, with a diameter of 32.72 meters and a height of 38 meters. It is entirely made of wood without using a single nail.", 
            116.406857, 39.882406, "建筑", 45, "/images/spots/qiniandian.jpg", 95, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(2, "圜丘坛", "Circular Mound Altar", 
            "圜丘坛在天坛的南部，是皇帝祭天的地方。整个祭坛全部用汉白玉石砌成，呈圆形，三层台基，象征天、地、人三才。", 
            "The Circular Mound Altar is located in the southern part of the Temple of Heaven. It was where emperors worshipped Heaven. The entire altar is made of white marble, circular in shape with three tiers, symbolizing heaven, earth, and humanity.", 
            116.407215, 39.873829, "祭坛", 25, "/images/spots/huanqiutan.jpg", 85, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(3, "皇穹宇", "Imperial Vault of Heaven", 
            "皇穹宇是圜丘祭坛北侧的一座圆形建筑，用来存放祭天神牌位。建筑周围有回音壁，是一种特殊的声学现象。", 
            "The Imperial Vault of Heaven is a circular building located to the north of the Circular Mound Altar. It was used to house the tablets of Heaven and ancestors. The building is surrounded by the Echo Wall, which exhibits a special acoustic phenomenon.", 
            116.407387, 39.877302, "建筑", 20, "/images/spots/huangqiongyu.jpg", 75, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(4, "丹陛桥", "Danbi Bridge", 
            "丹陛桥又称神桥，是一条南北向的神道，连接圜丘坛和皇穹宇。桥长360米，宽29米，是皇帝举行祭天大典时走的神路。", 
            "Danbi Bridge, also known as the Sacred Bridge, is a north-south ceremonial walkway connecting the Circular Mound Altar and the Imperial Vault of Heaven. The bridge is 360 meters long and 29 meters wide, and was used by emperors during grand ceremonies of heaven worship.", 
            116.407301, 39.875587, "通道", 15, "/images/spots/danbiqiao.jpg", 70, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(5, "七星石", "Seven-Star Stones", 
            "七星石位于祈年殿西北，由七块石头组成，象征北斗七星。古代帝王在此祭拜北斗星，祈求上天护佑。", 
            "The Seven-Star Stones are located to the northwest of the Hall of Prayer for Good Harvests. Composed of seven stones, they symbolize the Big Dipper. Ancient emperors worshipped the Big Dipper here, praying for heavenly protection.", 
            116.405784, 39.883467, "石刻", 10, "/images/spots/qixingshi.jpg", 60, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(6, "斋宫", "Palace of Abstinence", 
            "斋宫是皇帝祭天前斋戒沐浴的地方，位于天坛西北部。整个建筑群由宫墙、斋宫门、正殿、东西配殿等组成。", 
            "The Palace of Abstinence is where emperors fasted and bathed before worshipping Heaven. Located in the northwest part of the Temple of Heaven, the complex consists of palace walls, gates, the main hall, and east and west auxiliary halls.", 
            116.403942, 39.884327, "宫殿", 30, "/images/spots/zhaigong.jpg", 65, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(7, "回音壁", "Echo Wall", 
            "回音壁环绕在皇穹宇的四周，是一堵圆形的墙壁。因其特殊的声学原理，在壁的一边低声说话，在另一边就能听到，是著名的声学奇观。", 
            "The Echo Wall surrounds the Imperial Vault of Heaven. Due to its special acoustic principles, whispers can be heard on the opposite side of the circular wall, making it a famous acoustic marvel.", 
            116.407387, 39.877302, "景观", 15, "/images/spots/huiyingbi.jpg", 80, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(8, "三音石", "Three Echo Stones", 
            "三音石位于圜丘坛的中央，踏上中心石发出的声音可以产生三次回声，是天坛的又一声学奇观。", 
            "The Three Echo Stones are located in the center of the Circular Mound Altar. Stepping on the central stone produces three echoes, representing another acoustic marvel of the Temple of Heaven.", 
            116.407215, 39.873829, "石刻", 10, "/images/spots/sanyinshi.jpg", 75, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(9, "天坛公园东门", "East Gate of Temple of Heaven", 
            "天坛公园东门是游客常用的入口之一，靠近地铁站，交通便利。", 
            "The East Gate of the Temple of Heaven is one of the commonly used entrances for tourists, close to the subway station and convenient for transportation.", 
            116.412614, 39.881086, "入口", 5, "/images/spots/dongmen.jpg", 90, true, 0.0));
        
        sampleSpots.add(new ScenicSpot(10, "双环亭", "Double-Ring Pavilion", 
            "双环亭位于天坛东南部，是一座典型的明代亭子建筑，由两个环形围栏组成，是游客休息和欣赏园景的好去处。", 
            "The Double-Ring Pavilion is located in the southeast part of the Temple of Heaven. It is a typical Ming-dynasty pavilion with two circular railings, offering a great place for tourists to rest and enjoy the garden scenery.", 
            116.411541, 39.875626, "亭子", 10, "/images/spots/shuanghuanting.jpg", 55, true, 0.0));
        
        try {
            Files.createDirectories(filePath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), sampleSpots);
            logger.info("已创建示例景点数据");
        } catch (IOException e) {
            logger.error("创建示例景点数据失败", e);
        }
    }
    
    /**
     * 创建默认设置
     */
    private static void createDefaultSettings() {
        Path filePath = Paths.get(DATA_DIR, "settings.properties");
        
        Properties properties = new Properties();
        properties.setProperty("language", "zh");
        properties.setProperty("theme", "light");
        properties.setProperty("fontSize", "medium");
        properties.setProperty("defaultMapZoom", "1.0");
        properties.setProperty("showCrowdWarning", "true");
        properties.setProperty("autoSaveInterval", "300"); // 5分钟
        
        try (OutputStream output = Files.newOutputStream(filePath)) {
            properties.store(output, "TianTan Guide Default Settings");
            logger.info("已创建默认应用设置");
        } catch (IOException e) {
            logger.error("创建默认应用设置失败", e);
        }
    }
    
    /**
     * 创建示例图数据
     */
    private static void createSampleGraphData(SpotList spotList) {
        Path filePath = Paths.get(DATA_DIR, "graph.csv");
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            // 写入标题行
            writer.write("FromId,ToId,Weight,Type,IsCrowded\n");
            
            // 简单连接各个景点
            writer.write("1,3,300.0,WALKING,false\n");  // 祈年殿 -> 皇穹宇
            writer.write("3,1,300.0,WALKING,false\n");  // 皇穹宇 -> 祈年殿
            
            writer.write("3,2,350.0,WALKING,false\n");  // 皇穹宇 -> 圜丘坛
            writer.write("2,3,350.0,WALKING,false\n");  // 圜丘坛 -> 皇穹宇
            
            writer.write("3,4,250.0,WALKING,true\n");   // 皇穹宇 -> 丹陛桥
            writer.write("4,3,250.0,WALKING,true\n");   // 丹陛桥 -> 皇穹宇
            
            writer.write("4,2,150.0,WALKING,false\n");  // 丹陛桥 -> 圜丘坛
            writer.write("2,4,150.0,WALKING,false\n");  // 圜丘坛 -> 丹陛桥
            
            writer.write("1,5,180.0,WALKING,false\n");  // 祈年殿 -> 七星石
            writer.write("5,1,180.0,WALKING,false\n");  // 七星石 -> 祈年殿
            
            writer.write("1,6,300.0,WALKING,false\n");  // 祈年殿 -> 斋宫
            writer.write("6,1,300.0,WALKING,false\n");  // 斋宫 -> 祈年殿
            
            writer.write("3,7,10.0,WALKING,false\n");   // 皇穹宇 -> 回音壁
            writer.write("7,3,10.0,WALKING,false\n");   // 回音壁 -> 皇穹宇
            
            writer.write("2,8,10.0,WALKING,false\n");   // 圜丘坛 -> 三音石
            writer.write("8,2,10.0,WALKING,false\n");   // 三音石 -> 圜丘坛
            
            writer.write("9,1,500.0,WALKING,true\n");   // 东门 -> 祈年殿
            writer.write("1,9,500.0,WALKING,true\n");   // 祈年殿 -> 东门
            
            writer.write("9,10,600.0,WALKING,false\n"); // 东门 -> 双环亭
            writer.write("10,9,600.0,WALKING,false\n"); // 双环亭 -> 东门
            
            writer.write("10,2,450.0,WALKING,false\n"); // 双环亭 -> 圜丘坛
            writer.write("2,10,450.0,WALKING,false\n"); // 圜丘坛 -> 双环亭
            
            writer.write("5,6,250.0,WALKING,false\n");  // 七星石 -> 斋宫
            writer.write("6,5,250.0,WALKING,false\n");  // 斋宫 -> 七星石
            
            logger.info("已创建示例图数据");
        } catch (IOException e) {
            logger.error("创建示例图数据失败", e);
        }
    }
}