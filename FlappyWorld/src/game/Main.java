package game;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

	protected static Group root = null;
	private ImageView bkgrd = null ;
	private ImageView flappy = null;
	private Button button;
	private Text text = null;
	private static String[] Args;
	private Group scores;
	private ImageView clickRun = new ImageView("clickrun.png");
	private ImageView instruct = new ImageView("instructions.png");
	private ImageView getReady = new ImageView("getready.png");
	private ImageView gameOver = new ImageView("gameover.png");

	private Ground ground = null;
	private Obstacle pipe = new Obstacle("/obstacle_bottom.png", "/obstacle_top.png");
	private String url = getClass().getResource("/flappy.png").toString();
	private String URL = getClass().getResource("/flap.mp3").toString();
	private Media m = new Media(URL);
	private MediaPlayer player = new MediaPlayer(m);

	protected static int score = 0;
	
	static double g = 300;
	static final double boostV = -150;
	static final double sceneWidth=400;
	static final double sceneHeight=400;
	static final double start_x = 150, start_y = 150; // starting y position
	static final double max_y = sceneHeight*0.9-23; // end position
	static double v, duration, range;

	static Timeline timeline;
	static TranslateTransition transTransition;
	static Interpolator interpolator;

	static boolean endGame;

	private double calcTime(double distance, double velocity){
		return ((-velocity+Math.sqrt(velocity*velocity+(2*g*distance)))/g);
	}
	
	private void checkLocation(){
		double threshold = 8;
		if(flappy.getY()>=max_y-threshold || flappy.intersects(pipe.getX1(), pipe.getY1(), 52, 320) 
				|| flappy.intersects(pipe.getX2(), pipe.getY2(), 52, 320) ){ // end game when hits bottom or obstacle
			endGame=true;

		}
	}
	
	private void interpolator(){
		interpolator = new Interpolator(){
			@Override
			protected double curve (double t){
				checkLocation();
				text = new Text(Integer.toString(score));
				text.setLayoutX(20);
				text.setLayoutY(50);
				text.setFont(Font.loadFont(getClass().getResource("/Orbitron-Regular.ttf").toString(), 36));
			    root.getChildren().remove(scores);
			    scores.getChildren().clear();
			    scores.getChildren().add(text);
				root.getChildren().add(scores);
				if (flappy.getY()<=10 && endGame){ //if hits top, go to free fall
					range=max_y-flappy.getY();
					v=0;
					duration = calcTime(range,v);
				}
				if(endGame) {
					animationStop();
					flappyFly(0, Interpolator.LINEAR); 
				}
				double time = t * duration;
				double distance = (v*time)+(0.5*g*time*time);	
				return distance/range;
			}
		};
	}

	public void animationStop() {
			timeline.stop();
			ground.stop();
			pipe.stop();
			root.getChildren().add(gameOver);
			button = new Button("Restart");
			button.setLayoutX(150);
			button.setLayoutY(150);
			root.getChildren().add(button);
			addActionEventHandler();
	}
	
	public void addActionEventHandler() {
		 button.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                try {
						restartApplication();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        });
	}
	
	public void restartApplication() throws IOException {
		StringBuilder command = new StringBuilder();
        command.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
        for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            command.append(jvmArg + " ");
        }
        command.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
        command.append(Main.class.getName()).append(" ");
        for (String arg : Args) {
            command.append(arg).append(" ");
        }
        Runtime.getRuntime().exec(command.toString());
        System.exit(0);
	}
	
	public void flappyFly(double velocity, Interpolator i){
					range = max_y-flappy.getY();
					v=velocity;	
					duration = calcTime(range,v);
					timeline = new Timeline();
					KeyValue kv = new KeyValue(flappy.yProperty(),max_y,i);
					final KeyFrame kf = new KeyFrame(Duration.millis(duration * 1000), kv);	
					timeline.getKeyFrames().add(kf);
					timeline.play();
			}
	
	private void addMouseEventHandler(){
				root.onMouseClickedProperty().set(new EventHandler<MouseEvent>() {
					int n=0;
					@Override
					public void handle(MouseEvent event) {
						n++;
						if (n==1){
							root.getChildren().remove(clickRun);
							root.getChildren().addAll(instruct,getReady);
							ground.play();
						}
						else{
							root.getChildren().removeAll(instruct,getReady);
							if(timeline!=null){
								timeline.stop();
							}
							if(!endGame){
								player.play();
								pipe.play();
								flappyFly(boostV, interpolator);
							}
						}
					}
				});
			}

	@Override
	public void start(Stage primaryStage) throws Exception {
		//TODO 1: add background
		ground = new Ground("/ground.png");
		ground.movingGround(sceneHeight, sceneWidth);
		bkgrd = new ImageView("background2.png");
		clickRun.setLayoutX(sceneWidth/5.5);
		clickRun.setLayoutY(sceneHeight/5);
		getReady.setLayoutX(sceneWidth/4);
		getReady.setLayoutY(sceneHeight/5);
		instruct.setLayoutX(sceneWidth/3.5);
		instruct.setLayoutY(sceneHeight/2);
		gameOver.setLayoutX(sceneWidth/5);
		gameOver.setLayoutY(sceneHeight/5);
		text = new Text(Integer.toString(score));

	    //Background music
		String URl = getClass().getResource("/backgroundmusic.mp3").toString();
		Media m = new Media(URl);
		MediaPlayer Player = new MediaPlayer(m);
		Player.setCycleCount(MediaPlayer.INDEFINITE);
		Player.play();


		//TODO 2: add Flappy
		flappy = new ImageView(url);
		flappy.preserveRatioProperty().set(true);
		flappy.xProperty().set(start_x);
		flappy.yProperty().set(start_y);


		//Create a Group 
		root = new Group( );
		scores = new Group();
		root.getChildren().addAll(bkgrd, clickRun);
		root.getChildren().addAll(ground.getImageView(), pipe.getImageView1(), pipe.getImageView2());
		root.getChildren().add(flappy);
		scores.getChildren().add(text);
		root.getChildren().add(scores);

		//TODO 5: add mouse handler to the scene
		addMouseEventHandler();
		interpolator();
		pipe.movingGround(sceneHeight, sceneWidth);


		//Create scene and add to stage
		Scene scene = new Scene(root, sceneWidth, sceneHeight);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	public static void main(String[] args) {
		Args = args;
		Application.launch(args);
	}
	
	


}



