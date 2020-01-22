import java.io.*;
// import java.lang.reflect.Array;
import java.util.*;
import java.lang.Math;

public class parse{
    static String filepath = "Server/Logs/all_results.log";
    static File log = new File(filepath);
    public static void main(String[] args) throws FileNotFoundException{
        Scanner scanner = new Scanner(log);
        HashMap<String, ArrayList<String>> users = new HashMap<>();
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] b = line.split(" ");
            try{
                if(b[4].equals("\"POST")){
                    String[] c = b[5].split("/");
                    String[] d = c[2].split(":");
                    if(!users.containsKey(d[1])){
                        ArrayList<String> a = new ArrayList<>();
                        users.put(d[1], a);
                    }
                    if(users.containsKey(d[1])){
                        String e = d[0] + ":" + d[2] + ":" + d[4] + ":" + d[3];
                        //       timestamp     tabID       progress     website
                        //          0            1            2            3
                        users.get(d[1]).add(e);
                    }
                }
            }catch(Exception e){
                continue;
            }
        }
        
        HashMap<String, Double> percents = new HashMap<>();
        HashMap<String, ArrayList<Long>> overlaps = new HashMap<>();
        HashMap<String, ArrayList<Integer>> numSites = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> top_sites = new HashMap<>();
        for(String user:users.keySet()){
            top_sites.put(user, new HashMap<String, Integer>());
            int countChange = 0;
            int countOverlap = 0;
            boolean overlap = false;
            HashMap<String, String> open = new HashMap<>();
            Long beginLap = 0L;
            Long endLap = 0L;
            ArrayList<Long> time = new ArrayList<>();
            ArrayList<Integer> sitesPresent = new ArrayList<>();
            for(int i=0; i<users.get(user).size(); i++){
                String[] info = users.get(user).get(i).split(":");
                if(info[2].equals("0")){
                    open.remove(info[1]);
                }
                if(info.length < 4){
                    continue;
                }

                if((open.size()>1) && (overlap == true) && (info[2].equals("1"))){
                    countOverlap++;
                }
                
                if((overlap == true) && (open.size()>1) && (Long.parseLong(info[0]) > (beginLap + 20000)) && (countOverlap == 2)){
                    overlap = false;
                    countOverlap = 0;
                    open.clear();
                }

                if((info[3].equals("www.google.com")) && (info[2].equals("1"))){
                    if(!top_sites.get(user).containsKey(info[3])){
                        top_sites.get(user).put(info[3], 1);
                    }
                    else{
                        int temp = top_sites.get(user).get(info[3]) + 1;
                        top_sites.get(user).put(info[3], temp);
                    }
                    open.put(info[1], "closed");
                    countChange++;
                    continue;
                }

                if((info[3].equals("duckduckgo.com")) && (info[2].equals("1"))){
                    if(!top_sites.get(user).containsKey(info[3])){
                        top_sites.get(user).put(info[3], 1);
                    }
                    else{
                        int temp = top_sites.get(user).get(info[3]) + 1;
                        top_sites.get(user).put(info[3], temp);
                    }
                    open.put(info[1], "closed");
                    countChange++;
                    continue;
                }
                if(info[2].equals("1")){
                    if(!top_sites.get(user).containsKey(info[3])){
                        top_sites.get(user).put(info[3], 1);
                    }
                    else{
                        int temp = top_sites.get(user).get(info[3]) + 1;
                        top_sites.get(user).put(info[3], temp);
                    }
                    countChange++;
                    open.put(info[1], "open");
                }
                if(info[2].equals("2")){
                    open.remove(info[1]);
                }

                if((open.size()<2) && (overlap == true)){
                    endLap = Long.parseLong(info[0]);
                    overlap = false;
                    if ((endLap - beginLap) > 20000){
                        countOverlap = 0;
                        open.clear();
                        continue;
                    }
                    time.add(endLap - beginLap);
                    sitesPresent.add(countOverlap);
                    // System.out.println(sitesPresent);
                    countOverlap = 0;
                    continue;
                }
                if((open.size()>1) && (overlap == false)){
                    beginLap = Long.parseLong(info[0]);
                    overlap = true;
                    countOverlap += 2;
                }
            }
            overlaps.put(user, time);
            double percent = ((overlaps.get(user).size()-1)*100.0)/countChange;
            percents.put(user, percent);
            numSites.put(user, sitesPresent);
        }
        System.out.println("");

        for(String user: overlaps.keySet()){
            System.out.println("Number of overlaps for " + user + ": " + overlaps.get(user).size());
            // System.out.println(overlaps.get(user));

            double tot_time = 0;
            int num_overlaps = 0;
            ArrayList<Double> for_median = new ArrayList<>();
            for(int i=0; i<overlaps.get(user).size(); i++){
                num_overlaps++;
                tot_time += overlaps.get(user).get(i) / 1000.0;
                double temp = overlaps.get(user).get(i) / 1000.0;
                for_median.add(temp);
            }
            double avg_time = tot_time / num_overlaps;
            System.out.println("Average Time of overlaps was " + avg_time + " seconds");

            Collections.sort(for_median);
            double median = for_median.get(for_median.size() / 2);
            System.out.println("Median Time of overlaps was " + median + " seconds");

            double tot_site_overlap = 0.0;
            for(int j=0; j<numSites.get(user).size(); j++){
                tot_site_overlap += numSites.get(user).get(j);
            }
            double avg_sites_per_overlap = tot_site_overlap / num_overlaps;
            System.out.println("Average Number of sites in an overlap was " + avg_sites_per_overlap);

            System.out.println("Percent of visits being overlaps " + percents.get(user) + "%");

            // Standard Deviation
            double tot_x = 0;
            for(int x=0; x<overlaps.get(user).size(); x++){
                double temp = (overlaps.get(user).get(x)/1000.0) - avg_time;
                tot_x += temp * temp;
            }
            double std_dev = Math.sqrt(tot_x / (overlaps.get(user).size()-1));
            System.out.println("Standard Deviation of time was " + std_dev);

            int tot_visits = 0;
            for(String site: top_sites.get(user).keySet()){
                tot_visits += top_sites.get(user).get(site);
            }
            System.out.println("Number of total site visits: " + tot_visits);
            System.out.println("Total number of different sites visited by user: " + top_sites.get(user).size());
            
            // Need to order site by views
            ArrayList<String> site_name = new ArrayList<>();
            ArrayList<Integer> site_times = new ArrayList<>();
            for(String site: top_sites.get(user).keySet()){
                if(site_times.size() == 0){
                    String a = site + ": " + top_sites.get(user).get(site);
                    site_name.add(a);
                    site_times.add(top_sites.get(user).get(site));
                }
                int current_occur = top_sites.get(user).get(site);
                boolean changed = true;
                for(int i=0; i< site_times.size(); i++){
                    if((current_occur > site_times.get(i)) && changed){
                        String a = site + ": " + top_sites.get(user).get(site);
                        site_name.add(i, a);
                        site_times.add(i, top_sites.get(user).get(site));
                        changed = false;
                    }
                }
                if(changed){
                    String a = site + ": " + top_sites.get(user).get(site);
                    if(site_name.contains(a)){
                        continue;
                    }
                    site_name.add(a);
                    site_times.add(top_sites.get(user).get(site));
                }
            }
            System.out.println("Sites by number of visits: " + site_name);

            System.out.println("");
        }
        
        scanner.close();
    }
}
