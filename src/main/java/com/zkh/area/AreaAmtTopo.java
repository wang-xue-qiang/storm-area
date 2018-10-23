package com.zkh.area;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class AreaAmtTopo {
	public static void main(String[] args) throws Exception {
		TopologyBuilder  builder = new TopologyBuilder();
        SpoutConfig spoutConfig = new SpoutConfig(new ZkHosts("hadoop-senior.ibeifeng.com:2181"),"area_order","/area_order","area_order");
        builder.setSpout("spout",new KafkaSpout(spoutConfig),5);
        builder.setBolt("filter", new AreaFilterBolt(),5).shuffleGrouping("spout");
        builder.setBolt("areabolt", new AreaAmtBolt(),2).fieldsGrouping("filter",new Fields("area_id"));
        builder.setBolt("rsltbolt",new AreaRsltBolt(),1).shuffleGrouping("areabolt");
        //2、任务提交
        Config config = new Config();
        config.setDebug(true);
        StormTopology stormTopology = builder.createTopology();
        
        if(args != null && args.length > 0){
        	StormSubmitter.submitTopology(args[0], config, builder.createTopology());
        }else{
            //本地模式
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology("area_order",config,stormTopology);
        }
	}
}
