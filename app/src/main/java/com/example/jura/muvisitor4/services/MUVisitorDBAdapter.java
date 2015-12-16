package com.example.jura.muvisitor4.services;

/**
 * In class example
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.jura.muvisitor4.objects.RouteNode;


public class MUVisitorDBAdapter {

    private static final String DATABASE_NAME ="MUVisitor.db";
    private static final int DATABASE_VERSION = 1;


    private static final String TABLE_ROUTE = "tbl_route";
    public static final String ROUTE_ID = "route_id";
    public static final String ROUTE_NAME = "route_name";
    public static final String ROUTE_STATUS = "route_status";

    private static final String TABLE_NODE = "tbl_route_node";
    public static final String NODE_ID = "node_id";
    public static final String NODE_LAT = "node_latitude";
    public static final String NODE_LNG = "node_longtitude";
    public static final String NODE_NAME = "node_name";
    public static final String NODE_PREV = "node_prev";
    public static final String NODE_NEXT = "node_next";

    private static final String TABLE_PHOTO = "tbl_photo_at_node";
    public static final String PHOTO_ID = "photo_id";
    public static final String PHOTO_PATH = "photo_path";

    private SQLiteDatabase db;
    private final Context context;
    private MUVisitorDBOpenHelper dbHelper;
    public MUVisitorDBAdapter(Context context){
        this.context = context;
        //this.context.getResources().getString(R.string.database_name);
        dbHelper = new MUVisitorDBOpenHelper(this.context , DATABASE_NAME , null , DATABASE_VERSION);


    }
    /***
     * Open connection to the database
     */
    public void open(){
        try{
            db = dbHelper.getWritableDatabase();

        }catch (SQLiteException ex)
        {
            ex.printStackTrace();
        }
    }

    /***
     * Close connection to the database
     *
     */
    public void close(){
        db.close();
    }

    public Cursor listAllRoutes(){
        String query =
                " SELECT "+ "*" +
                " FROM "+ TABLE_ROUTE;
        return db.rawQuery(query, null);
    }

    public String getRouteName(int routeId){
        String query =
                " SELECT "+ ROUTE_NAME +
                " FROM "+ TABLE_ROUTE +
                " WHERE "+ ROUTE_ID + " = " + routeId;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            return cursor.getString(
                    cursor.getColumnIndex(MUVisitorDBAdapter.ROUTE_NAME)
            );
        }
        return "";
    }

    public Cursor getRouteInfoById(int routeId){
        String query =
                " SELECT "+ "*" +
                " FROM "+ TABLE_NODE +
                " WHERE "+ ROUTE_ID + " = " + routeId +
                " ORDER BY " + NODE_ID;
        return db.rawQuery(query, null);
    }

    public RouteNode getStartNode(int routeId){
        String query =
                " SELECT "+ "*" +
                " FROM "+ TABLE_NODE +
                " WHERE "+ ROUTE_ID + " = " + routeId +
                " AND "+ NODE_PREV + " IS NULL";
        return new RouteNode(db.rawQuery(query, null));
    }

    public RouteNode getNodeById(int nodeId){
        String query =
                " SELECT "+ "*" +
                " FROM "+ TABLE_NODE +
                " WHERE "+ NODE_ID + " = " + nodeId;
        return new RouteNode(db.rawQuery(query, null));
    }

    public void addPhoto(String path, int nodeId){
        path ="'"+path+"'";
        db.execSQL(
            "INSERT INTO `tbl_photo_at_node`(`photo_id`,`photo_path`,`node_id`) VALUES (NULL,"+path+","+nodeId+");"
        );
    }

    public Cursor getPhotoByRouteId(int routeId){
        String query =
                " SELECT "+ "*" +
                " FROM "+ TABLE_PHOTO + " p"+
                " INNER JOIN "+ TABLE_NODE + " n"+
                " ON "+ "p."+NODE_ID + " = " + "n."+NODE_ID +
                " WHERE "+ ROUTE_ID + " = " + routeId;
        return db.rawQuery(query, null);
    }

    public void deletePhotoById(int photoId){
        db.execSQL(
                "DELETE FROM `"+TABLE_PHOTO+"` WHERE `"+PHOTO_ID+"`= "+ photoId+";"
        );

    }

    public void updateRouteStatus(int routeId){
        String statement =
                "UPDATE `"+TABLE_ROUTE+"` SET `"+ROUTE_STATUS+"`= (" +
                "        SELECT CASE WHEN" +
                "        (" +
                "            SELECT COUNT(*) FROM (" +
                "                SELECT DISTINCT p."+NODE_ID+" FROM "+TABLE_PHOTO+" p" +
                "                INNER JOIN "+TABLE_NODE+" n ON p."+NODE_ID+" = n."+NODE_ID +
                "                WHERE "+ROUTE_ID+" = " + routeId +
                "            )" +
                "        )" +
                "            =" +
                "        (" +
                "            SELECT COUNT(*) FROM " + TABLE_NODE +
                "            GROUP BY "+ROUTE_ID+" HAVING "+ROUTE_ID+" = " + routeId +
                "         )" +
                "        THEN \"COMPLETE\" " +
                "        ELSE \"INCOMPLETE\" " +
                "        END AS isRouteComplete" +
                "    )" +
                "    WHERE `"+ROUTE_ID+"` = " + routeId;

        db.execSQL(statement);
    }

    //Add some neew route to db
    public void updateNewRoute(){
        //route3
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (7,13.79433,100.325777,NULL,8,3,'Office of President ');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (8,13.792721,100.326005,7,9,3,'International College');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (9,13.7904,100.321177,8,10,3,'Mahidol Sittakarn');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (10,13.788493,100.324106,9,11,3,'College of Musical');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (11,13.793968,100.321166,10,12,3,'Mahidol Learning Center');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (12,13.795594,100.320979,11,13,3,'MU Fitness');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (13,13.794458,100.324653,12,NULL,3,'MUICT');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route` VALUES (3,'Mahidol University Salaya','INCOMPLETE');"
        );

        //route4
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (14,51.500734,-0.124621,NULL,15,4,'Big Ben');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (15,51.503333,-0.118943,14,16,4,'London Eye');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (16,51.554848,-0.108193,15,17,4,'Emirate Stadium');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (17,51.481704,-0.190848,16,NULL,4,'Stamford Bridge');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route` VALUES (4,'London','INCOMPLETE');"
        );

        //route5
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (18,48.860593,2.337651,NULL,19,5,'Lourve Museum');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (19,48.858348,2.294499,18,NULL,5,'Eifel Tower');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route` VALUES (5,'Paris','INCOMPLETE');"
        );

        //route6
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (20,13.802877,100.622432,NULL,21,6,'Jura''s Home');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (21,13.779034,100.473578,20,22,6,'Tonson''s Home');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (22,13.765798,100.467969,21,NULL,6,'Kanchaporn''s Home');"
        );
        db.execSQL(
                "INSERT INTO `tbl_route` VALUES (6,'Dev Team Homes','INCOMPLETE');"
        );
    }

    public void emergency(){


        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_172108.jpg',7);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_172537.jpg',7);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_180325.jpg',9);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_180352.jpg',9);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_180403.jpg',9);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_180441.jpg',9);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_181050.jpg',10);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_181103.jpg',10);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_181153.jpg',10);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_181205.jpg',10);"
        );

        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_182055.jpg',8);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_182115.jpg',8);"
        );

        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_182710.jpg',12);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_182801.jpg',12);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_182836.jpg',12);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_182851.jpg',12);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_182908.jpg',12);"
        );

        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_183120.jpg',11);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_183159.jpg',11);"
        );
        db.execSQL(
                "INSERT INTO `tbl_photo_at_node`(`photo_path`,`node_id`) VALUES ('/storage/emulated/0/Pictures/MyCameraApp/IMG_20150507_183518.jpg',11);"
        );

    }

    /*
    UPDATE `tbl_route` SET `route_status`= (
        SELECT CASE WHEN
        (
            SELECT COUNT(*) FROM (
                SELECT DISTINCT p.node_id FROM tbl_photo_at_node p
                INNER JOIN tbl_route_node n ON p.node_id = n.node_id
                WHERE route_id = 1
            )
        )
            =
        (
            SELECT COUNT(*) FROM tbl_route_node
            GROUP BY route_id HAVING route_id = 1
         )
        THEN "COMPLETE"
        ELSE "INCOMPLETE"
        END AS isRouteComplete
    )
    WHERE `route_id`=1;

     */

    private static class MUVisitorDBOpenHelper extends SQLiteOpenHelper{

        public MUVisitorDBOpenHelper(Context context , String name, SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context,name,factory,version);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `tbl_route` (\n" +
                    "\t`route_id`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`route_name`\tTEXT NOT NULL  UNIQUE,\n" +
                    "\t`route_status`\tTEXT DEFAULT 'INCOMPLETE'\n" +
                    ");\n" );
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `tbl_route_node` (\n" +
                    "\t`node_id`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`node_latitude`\tREAL NOT NULL,\n" +
                    "\t`node_longtitude`\tREAL NOT NULL,\n" +
                    "\t`node_prev`\tINTEGER,\n" +
                    "\t`node_next`\tINTEGER,\n" +
                    "\t`route_id`\tINTEGER,\n" +
                    "\t`node_name`\tTEXT NOT NULL UNIQUE,\n" +
                    "\tFOREIGN KEY(`route_id`) REFERENCES tbl_route\n" +
                    ");" );
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `tbl_photo_at_node` (\n" +
                    "\t`photo_id`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`photo_path`\tTEXT NOT NULL,\n" +
                    "\t`node_id`\tINTEGER,\n" +
                    "\tFOREIGN KEY(`node_id`) REFERENCES tbl_route_node\n" +
                    ");\n");

            // tbl_route_node

            //route1
            db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (1,'13.794330','100.325777',NULL,'2',1,'Office of President');"
            );
            db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (2,'13.795339','100.325847',1,'3',1,'Faculty of Engineering');"
            );
            db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (3,'13.794453','100.324723',2,NULL,1,'Faculty of ICT');"
            );

            //route2

            db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (4,13.797998,100.327884,NULL,5,2,'7 Place');"
            );
            db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (5,13.798287,100.327428,4,6,2,'Seven Eleven');"
            );
            db.execSQL(
                "INSERT INTO `tbl_route_node` VALUES (6,13.798425,100.328372,5,NULL,2,'Loft');"
            );
            // tbl_route

            //route1
            db.execSQL(
                "INSERT INTO `tbl_route` VALUES (1,'first try','INCOMPLETE');"
            );
            //route2
            db.execSQL(
                "INSERT INTO `tbl_route` VALUES (2,'7 Place','INCOMPLETE');"
            );

            //route3
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (7,13.79433,100.325777,NULL,8,3,'Office of President ');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (8,13.792721,100.326005,7,9,3,'International College');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (9,13.7904,100.321177,8,10,3,'Mahidol Sittakarn');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (10,13.788493,100.324106,9,11,3,'College of Musical');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (11,13.793968,100.321166,10,12,3,'Mahidol Learning Center');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (12,13.795594,100.320979,11,13,3,'MU Fitness');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (13,13.794458,100.324653,12,NULL,3,'MUICT');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route` VALUES (3,'Mahidol University Salaya','INCOMPLETE');"
            );

            //route4
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (14,51.500734,-0.124621,NULL,15,4,'Big Ben');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (15,51.503333,-0.118943,14,16,4,'London Eye');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (16,51.554848,-0.108193,15,17,4,'Emirate Stadium');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (17,51.481704,-0.190848,16,NULL,4,'Stamford Bridge');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route` VALUES (4,'London','INCOMPLETE');"
            );

            //route5
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (18,48.860593,2.337651,NULL,19,5,'Lourve Museum');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (19,48.858348,2.294499,18,NULL,5,'Eifel Tower');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route` VALUES (5,'Paris','INCOMPLETE');"
            );

            //route6
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (20,13.802877,100.622432,NULL,21,6,'Jura''s Home');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (21,13.779034,100.473578,20,22,6,'Tonson''s Home');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (22,13.765798,100.467969,21,NULL,6,'Kanchaporn''s Home');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route` VALUES (6,'Dev Team Homes','INCOMPLETE');"
            );

        }

        public void init(){

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO auto-generated method stub


            //route3
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (7,13.79433,100.325777,NULL,8,3,'Office of President ');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (8,13.792721,100.326005,7,9,3,'International College');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (9,13.7904,100.321177,8,10,3,'Mahidol Sittakarn');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (10,13.788493,100.324106,9,11,3,'College of Musical');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (11,13.793968,100.321166,10,12,3,'Mahidol Learning Center');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (12,13.795594,100.320979,11,13,3,'MU Fitness');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (13,13.794458,100.324653,12,NULL,3,'MUICT');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route` VALUES (3,'Mahidol University Salaya','INCOMPLETE');"
            );

            //route4
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (14,51.500734,-0.124621,NULL,15,4,'Big Ben');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (15,51.503333,-0.118943,14,16,4,'London Eye');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (16,51.554848,-0.108193,15,17,4,'Emirate Stadium');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (17,51.481704,-0.190848,16,NULL,4,'Stamford Bridge');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route` VALUES (4,'London','INCOMPLETE');"
            );

            //route5
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (18,48.860593,2.337651,NULL,19,5,'Lourve Museum');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (19,48.858348,2.294499,18,NULL,5,'Eifel Tower');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route` VALUES (5,'Paris','INCOMPLETE');"
            );

            //route6
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (20,13.802877,100.622432,NULL,21,6,'Jura''s Home');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (21,13.779034,100.473578,20,22,6,'Tonson''s Home');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route_node` VALUES (22,13.765798,100.467969,21,NULL,6,'Kanchaporn''s Home');"
            );
            db.execSQL(
                    "INSERT INTO `tbl_route` VALUES (6,'Dev Team Homes','INCOMPLETE');"
            );

        }
    }





}





