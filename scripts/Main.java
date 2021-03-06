// references lab 2 work
// Populates msdInfo table for HBase Database
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.HashSet;
import java.util.Scanner;
import java.io.File;
import java.lang.String;

class Main{
	public static void main(String[] args){
		try{
			Configuration conf = HBaseConfiguration.create();
			HBaseAdmin admin = new HBaseAdmin(conf);
			
			//delete table if it's already been created before
			if(admin.tableExists("msdInfo")){
				admin.disableTable("msdInfo");
				admin.deleteTable("msdInfo");
			}
			
			//create table skeleton
			HTableDescriptor postsTableDescriptor = new HTableDescriptor("msdInfo");
            postsTableDescriptor.addFamily(new HColumnDescriptor("identifiers"));
            postsTableDescriptor.addFamily(new HColumnDescriptor("trackInfo"));
            admin.createTable(postsTableDescriptor);
            
            //open duplicates file and load into set
            Scanner in = new Scanner(new File("msd_duplicates.txt")); //
            String line = "";
            String[] splitLine;
            HashSet<String> duplicates = new HashSet<String>();
			while(in.hasNextLine()){
				line = in.nextLine();
				splitLine = line.split(" ");
				if (Character.isLetter(splitLine[0].charAt(0)))
					duplicates.add(splitLine[0]);
			}
			
			//populate table
			HTable msdInfoTable = new HTable(admin.getConfiguration(), "msdInfo");
			Scanner in = new Scanner(new File("temp.txt")); // **change source file name
			while(in.hasNextLine()){
				line = in.nextLine();
				// format: key - trackFileName - songID - artistName - songTitle
				splitLine = line.split(" ");
				Put putTrackInfo = new Put(Bytes.toBytes(splitLine[0]));
				if (!duplicates.contains(splitLine[1])) {
					populateRow(putTrackInfo,"identifiers","trackFileName",splitLine[1]);
					populateRow(putTrackInfo,"identifiers","songID",splitLine[2]);
					populateRow(putTrackInfo,"trackInfo","artistName",splitLine[3]);
					populateRow(putTrackInfo,"trackInfo","songTitle",splitLine[4]);
					
					msdInfoTable.put(putTrackInfo);
				}
			}
			msdInfoTable.flushCommits();
			msdInfoTable.close();
			
		} catch(IOException ex){
			System.out.println("IOException: " + ex.getMessage());
		} //catch (MasterNotRunningException ex){
			//System.out.println("MasterNotRunningException: " + ex.getMessage());
		//} catch (ZooKeeperConnectionException ex){
		//	System.out.println("ZooKeeperConnectionException: " + ex.getMessage());
		//}
	}
	
	public static void populateRow(Put put, String columnFamily, 
								   String columnQualifier, String value){
        put.add(Bytes.toBytes(columnFamily),
				Bytes.toBytes(columnQualifier),
				Bytes.toBytes(value));
    } 
}
