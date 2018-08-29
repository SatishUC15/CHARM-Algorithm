import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * CHARM algorithm implementation - Closed item set mining.
 */
public class CharmV2 {

    private Set<ItemSet> skipSet;

    public CharmV2() {
        this.skipSet = new TreeSet<>();
    }

    /**
     * Function to extract the items and transactions from an input .txt file.
     * Format: <String item, Set<Integer> transactions>
     *
     * @param ip
     * @param file
     */
    private void populateIpFromFile(Map<ItemSet, Set<Integer>> ip, String file) {
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
                    ItemSet temp = new ItemSet();
                    temp.add(x);
                    ip.put(temp, y);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to replace Xi with X.
     *
     * @param curr
     * @param target
     * @param map
     */
    private void replaceInItems(ItemSet curr, ItemSet target, Map<ItemSet, Set<Integer>> map) {
        List<ItemSet> temp = new ArrayList<>();
        // Identify the items to be replaced.
        for (ItemSet key : map.keySet()) {
            if (key.contains(curr)) {
                temp.add(key);
            }
        }
        // Update each item
        for (ItemSet key : temp) {
            Set<Integer> val = map.get(key);
            map.remove(key);
            key.addAll(target);
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
    private ItemSet charmProp(ItemSet xi, ItemSet xj, Set<Integer> y, int minSup, Map<ItemSet, Set<Integer>> nodes,
                             Map<ItemSet, Set<Integer>> newN) {
        if (y.size() >= minSup) {
            // temp = xi U xj
            ItemSet temp = new ItemSet();
            temp.addAll(xi);
            temp.addAll(xj);
            if (nodes.get(xi).equals(nodes.get(xj))) { // Property 1
                skipSet.add(xj);
                replaceInItems(xi, temp, newN);
                replaceInItems(xi, temp, nodes);
                return temp;
            } else if (nodes.getOrDefault(xj, new TreeSet<>()).containsAll(nodes.getOrDefault(xi, new TreeSet<>()))) { // Property 2
                replaceInItems(xi, temp, newN);
                replaceInItems(xi, temp, nodes);
                return temp;
            } else if (nodes.getOrDefault(xi, new TreeSet<>()).containsAll(nodes.getOrDefault(xj, new TreeSet<>()))) { // Property 3
                skipSet.add(xj);
                newN.put(temp, y);
            } else {
                if (!nodes.getOrDefault(xi, new TreeSet<>()).equals(nodes.getOrDefault(xj, new TreeSet<>()))) { // Property 4
                    newN.put(temp, y);
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
    private boolean isSubsumed(Map<ItemSet, Set<Integer>> c, Set<Integer> y) {
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
    private void charmExtended(Map<ItemSet, Set<Integer>> nodes, Map<ItemSet, Set<Integer>> c, int minSup) {
        List<ItemSet> items = new ArrayList(nodes.keySet());
        for (int idx1 = 0; idx1 < items.size(); idx1++) {
            ItemSet xi = items.get(idx1);
            if (skipSet.contains(xi)) continue;
            ItemSet x_prev = xi;
            Set<Integer> y;
            ItemSet x = null;
            Map<ItemSet, Set<Integer>> newN = new TreeMap<>();
            for (int idx2 = idx1 + 1; idx2 < items.size(); idx2++) {
                ItemSet xj = items.get(idx2);
                if (skipSet.contains(xj)) continue;
                // x = xi U xj
                x = new ItemSet();
                x.addAll(xi);
                x.addAll(xj);
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
    public Map<ItemSet, Set<Integer>> charm(Map<ItemSet, Set<Integer>> ip, int minSup) {
        // Eliminate items that don't satisfy the min support property
        for (ItemSet key : ip.keySet()) {
            if (ip.get(key).size() < minSup) {
                ip.remove(key);
            }
        }
        // Generate the closed item sets
        Map<ItemSet, Set<Integer>> c = new TreeMap<>();
        charmExtended(ip, c, minSup);
        return c;
    }

    public static void main(String[] args) {
        CharmV2 obj = new CharmV2();
        Map<ItemSet, Set<Integer>> ip = new TreeMap<>();
        //obj.populateIpFromFile(ip, "charmInNumber.txt");
        obj.populateIpFromFile(ip, "numbertest.txt");
        //obj.populateIpFromFile(ip, "range2_new.txt");
        //System.out.println(ip);
        Map<ItemSet, Set<Integer>> c = obj.charm(ip, 1);
        //System.out.println(c);
        c.forEach((k,v)->
        {
            if(k.itemSet.size()>=1 && v.size()>=1)
            {
                System.out.print(k+"\t");
                System.out.println(v);
            }

        });
    }
}

/**
 * ItemSet : A comparable treeset (incomplete)
 */
class ItemSet implements Comparable<ItemSet>{
    Set<String> itemSet;

    public ItemSet() {
        itemSet = new TreeSet<>();
    }

    public void add(String item) {
        itemSet.add(item);
    }

    public void addAll(ItemSet itemSet2) {
        itemSet.addAll(itemSet2.itemSet);
    }

    public boolean contains(ItemSet itemSet2) {
        return itemSet.containsAll(itemSet2.itemSet);
    }

    @Override
    public int compareTo(ItemSet o) {
        return itemSet.toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return itemSet.toString();
    }
}