package org.apache.solr.schema;
public class GraghSingleton{
        private static Graph instance = null;
        protected GraghSingleton(){
            // Exists only to defeat instantiation
        }
        public static Graph getInstance(){
                if(instance == null){
                        instance = new TinkerGraph();
                }
                return instance;
        } 
}

