import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class WordGraph {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath;

        //接受文件路径作为参数或通过用户输入接受
        if (args.length == 1) {
            filePath = args[0];
        } else {
            filePath = "C:\\Users\\yhh68\\Desktop\\새 텍스트 문서.txt";
        }

        // 读入text文件
        String text = readFile(filePath);
        if (text == null) {
            System.err.println("파일을 읽을 수 없습니다.");
            return;
        }

        //分析单词并生成有向图
        Map<String, Map<String, Integer>> graph = buildGraph(text);

        //将图导出为DOT文件
        exportToDot(graph, "graph.dot");

        //使用Graphviz将DOT文件转换为PNG文件
        generateImageFromDot("graph.dot", "graph.png");

        // menu
        while (true) {
            System.out.println("请选择您想要的操作:");
            System.out.println("1. 查找两个单词之间的最短路径");
            System.out.println("2. 生成新文本");
            System.out.println("3. 随机遍历图");
            System.out.println("q. 结束");
            System.out.print("选择: ");
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("q")) {
                break;
            }

            switch (choice) {
                case "1":
                    System.out.print("请输入两个单词（例如：word1 word2）： ");
                    String[] words = scanner.nextLine().split("\\s+");
                    if (words.length != 2) {
                        System.out.println("输入错误，请重新输入");
                        break;
                    }
                    findShortestPathsAndDisplay(graph, words[0], words[1]);
                    break;
                case "2":
                    System.out.print("请输入新文本： ");
                    String newText = scanner.nextLine();
                    System.out.println("新文本： " + generateNewText(graph, newText));
                    break;
                case "3":
                    System.out.println("开始图搜索.");
                    randomGraphTraversal(graph);
                    break;
                default:
                    System.out.println("输入错误.");
                    break;
            }
        }
    }

    //读取文件并返回文本
    private static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return content.toString();
    }

    // 分析文本并生成有向图
    private static Map<String, Map<String, Integer>> buildGraph(String text) {
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
        Map<String, Map<String, Integer>> graph = new HashMap<>();

        for (int i = 0; i < words.length - 1; i++) {
            String wordA = words[i];
            String wordB = words[i + 1];

            graph.putIfAbsent(wordA, new HashMap<>());
            Map<String, Integer> edges = graph.get(wordA);
            edges.put(wordB, edges.getOrDefault(wordB, 0) + 1);
        }

        return graph;
    }

    // 将图导出为DOT文件
    private static void exportToDot(Map<String, Map<String, Integer>> graph, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("digraph G {\n");

            for (String node : graph.keySet()) {
                for (Map.Entry<String, Integer> edge : graph.get(node).entrySet()) {
                    int weight = edge.getValue();
                    writer.write("  \"" + node + "\" -> \"" + edge.getKey() + "\" [label=\"" + weight + "\", weight=" + weight + "];\n");
                }
            }

            writer.write("}\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// 使用Graphviz将DOT文件转换为PNG文件

    private static void generateImageFromDot(String dotFilePath, String outputFilePath) {
        try {
            // 使用绝对路径执行Graphviz的dot命令
            String graphvizPath = "C:\\Users\\yhh68\\Downloads\\windows_10_cmake_Release_Graphviz-11.0.0-win64\\Graphviz-11.0.0-win64\\bin\\dot.exe"; //这个路径需要根据各自计算机的路径进行修改
            ProcessBuilder processBuilder = new ProcessBuilder(graphvizPath, "-Tpng", dotFilePath, "-o", outputFilePath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //查找bridge words的
    private static void findBridgeWords(Map<String, Map<String, Integer>> graph, String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            System.out.println("No " + word1 + " or " + word2 + " in the graph!");
            return;
        }

        Set<String> bridgeWords = new HashSet<>();
        Map<String, Integer> word1Edges = graph.get(word1);

        for (String word3 : word1Edges.keySet()) {
            if (graph.containsKey(word3) && graph.get(word3).containsKey(word2)) {
                bridgeWords.add(word3);
            }
        }

        if (bridgeWords.isEmpty()) {
            System.out.println("No bridge words from " + word1 + " to " + word2 + "!");
        } else {
            System.out.print("The bridge words from " + word1 + " to " + word2 + " are: ");
            System.out.println(String.join(", ", bridgeWords) + ".");
        }
    }

    // 生成新文本
    private static String generateNewText(Map<String, Map<String, Integer>> graph, String inputText) {
        String[] words = inputText.toLowerCase().split("\\s+");
        StringBuilder newText = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            newText.append(word1).append(" ");

            Set<String> bridgeWords = new HashSet<>();
            if (graph.containsKey(word1)) {
                Map<String, Integer> word1Edges = graph.get(word1);
                for (String word3 : word1Edges.keySet()) {
                    if (graph.containsKey(word3) && graph.get(word3).containsKey(word2)) {
                        bridgeWords.add(word3);
                    }
                }
            }

            if (!bridgeWords.isEmpty()) {
                List<String> bridgeWordList = new ArrayList<>(bridgeWords);
                Collections.shuffle(bridgeWordList);
                newText.append(bridgeWordList.get(0)).append(" ");
            }
        }

        newText.append(words[words.length - 1]);
        return newText.toString();
    }

    // 查找并输出最短路径
    private static void findShortestPathsAndDisplay(Map<String, Map<String, Integer>> graph, String word1, String word2) {
        // If start or end word not in the graph, print message and return
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            System.out.println("No path exists between " + word1 + " and " + word2 + ".");
            return;
        }

        // Get the shortest path
        List<String> shortestPath = findShortestPath(graph, word1, word2);

        // If no shortest path found, print message and retur1n
        if (shortestPath.isEmpty()) {
            System.out.println("No shortest path exists between " + word1 + " and " + word2 + ".");
            return;
        }

        // Output the shortest path
        System.out.println("Shortest path from " + word1 + " to " + word2 + ":");
        System.out.println(String.join(" -> ", shortestPath));
    }
    // 查找起始词和目标词之间的最短路径的函数
    private static List<String> findShortestPath(Map<String, Map<String, Integer>> graph, String start, String end) {
        Set<String> visited = new HashSet<>();
        Queue<List<String>> queue = new LinkedList<>();

        List<String> initialPath = new ArrayList<>();
        initialPath.add(start);
        queue.offer(initialPath);

        while (!queue.isEmpty()) {
            List<String> currentPath = queue.poll();
            String current = currentPath.get(currentPath.size() - 1);

            if (current.equals(end)) {
                return currentPath;
            } else {
                Map<String, Integer> neighbors = graph.getOrDefault(current, Collections.emptyMap());
                for (String neighbor : neighbors.keySet()) {
                    if (!visited.contains(neighbor)) {
                        List<String> newPath = new ArrayList<>(currentPath);
                        newPath.add(neighbor);
                        queue.offer(newPath);
                        visited.add(neighbor);
                    }
                }
            }
        }

        // If no path found, return an empty list
        return new ArrayList<>();
    }

    // 递归调用以查找最短路径的函数
    private static void findShortestPathsUtil(Map<String, Map<String, Integer>> graph, String current, String end, Set<String> visited, Deque<String> currentPath, List<List<String>> shortestPaths) {
        // 将当前节点标记为已访问
        visited.add(current);

        // 到达目标节点时，将当前路径添加到最短路径列表
        if (current.equals(end)) {
            shortestPaths.add(new ArrayList<>(currentPath));
        } else {
            // 对当前节点的所有相邻节点进行递归调用
            Map<String, Integer> neighbors = graph.getOrDefault(current, Collections.emptyMap());
            for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
                String neighbor = neighborEntry.getKey();
                if (!visited.contains(neighbor)) {
                    currentPath.addLast(neighbor);
                    findShortestPathsUtil(graph, neighbor, end, visited, currentPath, shortestPaths);
                    currentPath.removeLast();
                }
            }
        }

        // 移除当前节点的标记，使其可以再次被访问，以不影响其他路径
        visited.remove(current);
    }

    // 在图中进行随机搜索的函数
    private static void randomGraphTraversal(Map<String, Map<String, Integer>> graph) {
        Random random = new Random();
        List<String> nodes = new ArrayList<>(graph.keySet());
        String startNode = nodes.get(random.nextInt(nodes.size()));

        Set<String> visited = new HashSet<>();
        Deque<String> path = new ArrayDeque<>();
        path.addLast(startNode);
        visited.add(startNode);

        StringBuilder traversalPath = new StringBuilder(startNode);

        System.out.println("Starting node: " + startNode);

        while (!path.isEmpty()) {
            String currentNode = path.getLast();
            Map<String, Integer> edges = graph.get(currentNode);

            // 如果当前节点不存在于图中，则停止搜索
            if (edges == null) {
                break;
            }

            List<String> possibleNodes = new ArrayList<>();

            for (String nextNode : edges.keySet()) {
                if (!visited.contains(nextNode)) {
                    possibleNodes.add(nextNode);
                }
            }

            if (!possibleNodes.isEmpty()) {
                String nextNode = possibleNodes.get(random.nextInt(possibleNodes.size()));
                path.addLast(nextNode);
                visited.add(nextNode);
                traversalPath.append(" -> ").append(nextNode);
            } else {
                path.removeLast();
            }
        }

        System.out.println("Graph traversal completed.");
        System.out.println(traversalPath.toString());
    }
}