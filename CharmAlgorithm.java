import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * CHARM algorithm implementation - Closed item set mining.
 */
public class CharmAlgorithm {

    private Set<String> skipSet;

    public CharmAlgorithm() {
        this.skipSet = new TreeSet<>();
    }

    /**
     * Function to extract the items and transactions from an input .txt file.
     * Format: <String item, Set<Integer> transactions>
     *
     * @param ip
     * @param file
     */
    private void populateIpFromFile(Map<String, Set<Integer>> ip, String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                String x = line.split("\t")[0];
                String trans = line.split("\t")[1];

                Set<Integer> y = new TreeSet<>();
                for (String s : trans.split(",")) {
                    y.add(Integer.parseInt(s));
                }

                if (x != null && !y.isEmpty()) {
                    ip.put(x, y);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to compute the union of two item sets - (Xi U Xj). Items are ordered lexicographically.
     *
     * @param str1
     * @param str2
     * @return Xi U Xj
     */
    private String getStringUnion(String str1, String str2) {
        String str = str1 + str2;
        char[] strArray = str.toCharArray();
        Arrays.sort(strArray);
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < strArray.length - 1; idx++) {
            if (strArray[idx] != strArray[idx + 1]) {
                sb.append(strArray[idx]);
            }
        }
        sb.append(strArray[strArray.length - 1]);
        return sb.toString();
    }

    /**
     * Function to replace Xi with X.
     *
     * @param curr
     * @param target
     * @param map
     */
    private void replaceInItems(String curr, String target, Map<String, Set<Integer>> map) {
        List<String> temp = new ArrayList<>();
        // Identify the items to be replaced.
        for (String key : map.keySet()) {
            if (key.contains(curr)) {
                temp.add(key);
            }
        }
        // Update each item
        for (String key : temp) {
            Set<Integer> val = map.get(key);
            map.remove(key);
            key = key.replace(curr, target);
            // Sort the items in each set
            key = getStringUnion(key, "");
            map.put(key, val);
        }
    }

    /**
     * Incorporating the 4 charm properties.
     *
     * @param xi
     * @param xj
     * @param y
     * @param minSup
     * @param nodes
     * @param newN
     * @return xi
     */
    private String charmProp(String xi, String xj, Set<Integer> y, int minSup, Map<String, Set<Integer>> nodes,
                             Map<String, Set<Integer>> newN) {
        if (y.size() >= minSup) {
            if (nodes.get(xi).equals(nodes.get(xj))) { // Property 1
                skipSet.add(xj);
                String temp = getStringUnion(xi, xj);
                replaceInItems(xi, temp, newN);
                replaceInItems(xi, temp, nodes);
                return temp;
            } else if (nodes.getOrDefault(xj, new TreeSet<>()).containsAll(nodes.getOrDefault(xi, new TreeSet<>()))) { // Property 2
                String temp = getStringUnion(xi, xj);
                replaceInItems(xi, temp, newN);
                replaceInItems(xi, temp, nodes);
                return temp;
            } else if (nodes.getOrDefault(xi, new TreeSet<>()).containsAll(nodes.getOrDefault(xj, new TreeSet<>()))) { // Property 3
                skipSet.add(xj);
                newN.put(getStringUnion(xi, xj), y);
            } else {
                if (!nodes.getOrDefault(xi, new TreeSet<>()).equals(nodes.getOrDefault(xj, new TreeSet<>()))) { // Property 4
                    newN.put(getStringUnion(xi, xj), y);
                }
            }
        }
        return xi;
    }

    /**
     * Function to check if an item set is subsumed by the existing item sets.
     *
     * @param c
     * @param y
     * @return true/false
     */
    private boolean isSubsumed(Map<String, Set<Integer>> c, Set<Integer> y) {
        for (Set<Integer> val : c.values()) {
            if (val.equals(y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Charm - Extended routine
     *
     * @param nodes
     * @param c
     * @param minSup
     */
    private void charmExtended(Map<String, Set<Integer>> nodes, Map<String, Set<Integer>> c, int minSup) {
        List<String> items = new ArrayList(nodes.keySet());
        for (int idx1 = 0; idx1 < items.size(); idx1++) {
            String xi = items.get(idx1);
            if (skipSet.contains(xi)) continue;
            String x_prev = xi;
            Set<Integer> y;
            String x = null;
            Map<String, Set<Integer>> newN = new TreeMap<>();
            for (int idx2 = idx1 + 1; idx2 < items.size(); idx2++) {
                String xj = items.get(idx2);
                if (skipSet.contains(xj)) continue;
                x = getStringUnion(xi, xj);
                y = nodes.getOrDefault(xi, new TreeSet<>());
                Set<Integer> temp = new TreeSet<>();
                temp.addAll(y);
                temp.retainAll(nodes.getOrDefault(xj, new TreeSet<>()));
                xi = charmProp(xi, xj, temp, minSup, nodes, newN);
            }
            if (!newN.isEmpty()) {
                charmExtended(newN, c, minSup);
            }
            if (x_prev != null && nodes.get(x_prev) != null && !isSubsumed(c, nodes.get(x_prev))) {
                c.put(x_prev, nodes.get(x_prev));
            }
            if (x != null && nodes.get(x) != null && !isSubsumed(c, nodes.get(x))) {
                c.put(x, nodes.get(x));
            }
        }
    }

    /**
     * CHARM routine - Items are ordered lexicographically.
     *
     * @param ip
     * @param minSup
     * @return c
     */
    public Map<String, Set<Integer>> charm(Map<String, Set<Integer>> ip, int minSup) {
        // Eliminate items that don't satisfy the min support property
        for (String key : ip.keySet()) {
            if (ip.get(key).size() < minSup) {
                ip.remove(key);
            }
        }
        // Generate the closed item sets
        Map<String, Set<Integer>> c = new TreeMap<>();
        charmExtended(ip, c, minSup);
        return c;
    }

    public static void main(String[] args) {
        CharmAlgorithm obj = new CharmAlgorithm();
        Map<String, Set<Integer>> ip = new TreeMap<>();
        obj.populateIpFromFile(ip, "charmIn.txt");
        System.out.println(ip);
        Map<String, Set<Integer>> c = obj.charm(ip, 3);
        System.out.println(c);
    }
}
