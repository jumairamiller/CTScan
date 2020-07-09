import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.*;

import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;

/**
 * @author 	Jumaira Miller
 * @version 1.0
 * @since 	21/02/2020
**/

public class ImageProcessingCode extends Application {
	short cthead[][][]; //store the 3D volume for displayed data set; this refers to other two arrays depending on toggle
	short eqcthead[][][]; //stores the 3D volume dataset of equalized image
	short ogcthead[][][]; //stores the 3D volume data of original dataset

	short min, max; //min/max value in the 3D volume data set
	short ogMin, ogMax;
	short eqMin = 0;
	short eqMax = 255;



	@Override
	public void start(Stage stage) throws FileNotFoundException, IOException {
		stage.setTitle("CThead Viewer");

		ReadData();
		cthead = ogcthead;
		max = ogMax;
		min = ogMin;
		equalize();

		int width = 256;
		int height = 256;
		int depth = 113;

		WritableImage medical_top = new WritableImage(width, height); //Create new image
		ImageView imageViewTop = new ImageView(medical_top); //Instantiating new view

		WritableImage medical_front = new WritableImage(width, depth);
		ImageView imageViewFront = new ImageView(medical_front);

		WritableImage medical_side = new WritableImage(width, depth);
		ImageView imageViewSide = new ImageView(medical_side);

		ToggleButton mip_button = new ToggleButton("MIP"); //button to switch to MIP mode
		ToggleButton hist_eq_button = new ToggleButton ("Equalize"); //button to switch to Equalized view

		/**
		 * sliders to step through the slices (x, y, and z directions)
		 */
		Slider zslider = new Slider(0, 112, 0); //this slider runs through slides top to bottom
		Slider yslider = new Slider(0, 255, 0); //this slider runs through slides front to back
		Slider xslider = new Slider(0, 255, 0); //this slider runs through slides side to side

		/**
		 * Two sliders to adjust how many pixels the image is resized to
		 */
		Slider resizeSliderNN = new Slider(1, 512, 1);
		Slider resizeSliderBI = new Slider(1, 512, 1);

		mip_button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MIPTop2Bottom((WritableImage) imageViewTop.getImage(), (int) zslider.getValue(), mip_button.isSelected());
				MIPFront2Back((WritableImage) imageViewFront.getImage(), (int) yslider.getValue(), mip_button.isSelected());
				MIPSide2Side((WritableImage) imageViewSide.getImage(), (int) xslider.getValue(), mip_button.isSelected());
			}
		});

		hist_eq_button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (hist_eq_button.isSelected()) {
					cthead = eqcthead;
					min = eqMin;
					max = eqMax;
				} else {
					cthead = ogcthead;
					min = ogMin;
					max = ogMax;
				}
			}
		});

		/**
		 * a listener that outputs the current slice being read for z
		 */
		zslider.valueProperty().addListener(
				new ChangeListener<Number>() {
					public void changed(ObservableValue<? extends Number>
												observable, Number oldValue, Number newValue) {
						//updating outputted slides according to sliders
						System.out.println((int) zslider.getValue()); //testing purposes, prints values in terminal
						MIPTop2Bottom((WritableImage) imageViewTop.getImage(), (int) zslider.getValue(), false);
					}
				});

		/**
		 * a listener that outputs the current slice being read for y
		 */
		yslider.valueProperty().addListener(
				new ChangeListener<Number>() {
					public void changed(ObservableValue<? extends Number>
												observable, Number oldValue, Number newValue) {
						//updating outputted slides according to sliders
						System.out.println((int) yslider.getValue()); //testing purposes, prints values in terminal
						MIPFront2Back((WritableImage) imageViewFront.getImage(), (int) yslider.getValue(), false);
					}
				});

		/**
		 * a listener that outputs the current slice being read for x
		 */
		xslider.valueProperty().addListener(
				new ChangeListener<Number>() {
					public void changed(ObservableValue<? extends Number>
												observable, Number oldValue, Number newValue) {

						System.out.println((int) xslider.getValue()); //testing purposes, prints values in terminal
						MIPSide2Side((WritableImage) imageViewSide.getImage(), (int) xslider.getValue(), false);
					}
				});

		resizeSliderNN.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						WritableImage medical_front_NN = resize(medical_front, newValue.intValue(), newValue.intValue(), false); //creates a writable image to store the resized image from the slider values
						imageViewFront.setImage(medical_front_NN); //sets the image on the screen to the new resized image

						WritableImage medical_top_NN = resize(medical_top, newValue.intValue(), newValue.intValue(), false);//intValue reads in the newValue of slider as an integer; slider takes in double values so it needs to be parsed
						imageViewTop.setImage(medical_top_NN);

						WritableImage medical_side_NN = resize(medical_side, newValue.intValue(), newValue.intValue(), false);
						imageViewSide.setImage(medical_side_NN);
					}
				});

		resizeSliderBI.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						WritableImage medical_front_BI = resize(medical_front, newValue.intValue(), newValue.intValue(), true); //creates a writable image to store the resized image from the slider values
						imageViewFront.setImage(medical_front_BI); //sets the image on the screen to the new resized image

						WritableImage medical_top_BI = resize(medical_top, newValue.intValue(), newValue.intValue(), true);//intValue reads in the newValue of slider as an integer; slider takes in double values so it needs to be parsed
						imageViewTop.setImage(medical_top_BI);

						WritableImage medical_side_BI = resize(medical_side, newValue.intValue(), newValue.intValue(), true);
						imageViewSide.setImage(medical_side_BI);
					}
				});


		/**
		 * The following JavaFX code formats how the elements are displayed on screen
		 */
		GridPane root = new GridPane();

		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(33);

		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(33);

		ColumnConstraints column3 = new ColumnConstraints();
		column3.setPercentWidth(33);

		root.getColumnConstraints().addAll(column1, column2, column3);

		RowConstraints row0 = new RowConstraints();
		row0.setPercentHeight(5);

		RowConstraints row1 = new RowConstraints();
		row1.setPercentHeight(5);

		RowConstraints row2 = new RowConstraints();
		row2.setPercentHeight(80);


		RowConstraints row3 = new RowConstraints();
		row3.setPercentHeight(10);

		root.getRowConstraints().addAll(row0, row1,row2,row3);

		BorderPane imageFrameTop = new BorderPane();
		imageFrameTop.setCenter(imageViewTop);

		BorderPane imageFrameFront = new BorderPane();
		imageFrameFront.setCenter(imageViewFront);

		BorderPane imageFrameSide = new BorderPane();
		imageFrameSide.setCenter(imageViewSide);

		root.setConstraints(hist_eq_button, 1, 0);
		root.setConstraints(resizeSliderNN, 0, 1);
		root.setConstraints(mip_button, 1, 1);
		root.setConstraints(resizeSliderBI, 2, 1);
		root.setConstraints(imageFrameTop, 0, 2);
		root.setConstraints(imageFrameFront, 1,2);
		root.setConstraints(imageFrameSide, 2, 2);
		root.setConstraints(zslider, 0, 3);
		root.setConstraints(yslider, 1, 3);
		root.setConstraints(xslider, 2, 3);

		root.getChildren().addAll(imageFrameTop, imageFrameFront, imageFrameSide, mip_button,
				hist_eq_button, zslider, yslider, xslider, resizeSliderNN, resizeSliderBI);

		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Function to read in the cthead data set
	 */
	public void ReadData() throws IOException {
		//File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
		File file = new File("CThead");
		//Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k; //loop through the 3D data set

		ogMin = Short.MAX_VALUE;
		ogMax = Short.MIN_VALUE; //set to extreme values
		System.out.println(ogMin + " " + ogMax);
		short read; //value read in
		int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

		ogcthead = new short[113][256][256]; //allocate the memory - note this is fixed for this data set
		//loop through the data reading it in
		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {
					//because the Endianess is wrong, it needs to be read byte at a time and swapped
					b1 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
					b2 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
					read = (short) ((b2 << 8) | b1); //and swizzle the bytes around
					if (read < ogMin) ogMin = read; //update the minimum
					if (read > ogMax) ogMax = read; //update the maximum
					ogcthead[k][j][i] = read; //put the short into memory
				}
			}
		}
		System.out.println(ogMin + " " + ogMax); //diagnostic - for CThead this should be -1117, 2248
		//(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
		//therefore histogram equalization would be a good thing
	}

	/**
	 * Calculates a histogram of the CT head dataset
	 */
	public int[] getHistogram() {
		int greyLevels = (max - min) + 1;
		int[] histogram = new int[greyLevels]; //3366 levels
		//Each bin(array index representing grey levels) of histogram is automatically initialized to 0 in java


		//z axis of cthead (top down)
		for (int k = 0; k < 113; k++) {
			//y axis of cthead (front to back)
			for (int j = 0; j < 256; j++) {
				//x axis of cthead (side to side)
				for (int i = 0; i < 256; i++) {
					//for each pixel in each slice, the histogram value at the index is updated
					int index = cthead[k][j][i] - min; //cant have negative index so you -min to shift everything by |min| to get indexs from 0
					histogram[index]++;
				}
			}
		}
		return histogram;
	}

	/**
	 * Using the histogram, calculates the cumulative distribution and mapping to new values
	 */
	public int[] cumulativeDistAndMapping(int[] histogram) {
		int[] cumulativeDist = new int[histogram.length];
		int[] mapping = new int[cumulativeDist.length];

		double size = 113 * 256 * 256; //size is the number of voxels the dataset contains

		cumulativeDist[0] = histogram[0];
		for (int i = 1; i < cumulativeDist.length; i++) {
			cumulativeDist[i] = cumulativeDist[i - 1] + histogram[i];
			//maps the values in the cthead range to the new range [0,255]
			//mapping takes the index in the histogram and converts its to a value between the compressed colour range
			mapping[i] = ((int) (255.0 * (cumulativeDist[i]/size)));
		}
		return mapping;
	}

	/**
	 * this function takes the mapping of values from the original cthead and stores
	 * it in a reference for the equalized ct head
	 * (i.e. goes through the original cthead, takes the values at each position and finds
	 * its mapping value and stores it in the eqcthead reference array )
	 *
	 * this method is void because I created these arrays outside so they can be directly accessed
	 */
	public void equalize() {
		eqcthead = new short[ogcthead.length][ogcthead[0].length][ogcthead[0][0].length]; //Same dimentions as the original dataset
		int[] mapping = cumulativeDistAndMapping(getHistogram()); //store the values returned from the mapping

		for (int k = 0; k < 113; k++) {
			//y axis of cthead (front to back)
			for (int j = 0; j < 256; j++) {
				//x axis of cthead (side to side)
				for (int i = 0; i < 256; i++) {
					short datum = ogcthead[k][j][i];
					int col = mapping[datum-min];
					eqcthead[k][j][i] = (short) col;
				}
			}
		}
	}


	public WritableImage resize(WritableImage image, int newWidth, int newHeight, boolean isBilinear) {
		WritableImage newImage = new WritableImage(newWidth,newHeight); //new Writable image initialized for resized image
		PixelWriter writer = newImage.getPixelWriter();	//writing to newImage
		PixelReader reader = image.getPixelReader();	//Reading from original image

		// Calculating Nearest Neighbour Resize of image
		if(!isBilinear){
			//For each pixel i and j is a value such that i or j multiplied by the ratio
			for (int j = 0; j < newHeight -1; j++) {
				for (int i = 0; i < newWidth -1; i++) {
					//find the resized x and y values of the new image
					//placeholder x and y to pass to reader to store read in values for x and y from original image
					int y = (int) (j * (image.getHeight()/newHeight));
					int x = (int) (i * (image.getWidth()/newWidth));
					//set colour at resized position to colour at position from original image
					writer.setColor(i, j, reader.getColor(x,y));
				}
			}
		}

		// Calculating the bilinear resized image
		if(isBilinear){
			for (int j = 0; j < newHeight -1; j++) {
				for (int i = 0; i < newWidth - 1; i++) {
					//find the original pixel position of the current pixel of resized image
					double y = (double) j / newHeight * (image.getHeight() - 1);
					double x = (double) i / newWidth * (image.getWidth() - 1);

					//casting the above two to int type now
					int ogY = (int) Math.floor(y);
					int ogX = (int) Math.floor(x);

					//finding colour values 4 pixels around the x,y from original pixel position
					double p1 = reader.getColor(ogX,ogY).getRed();
					double p2 = reader.getColor(ogX + 1,ogY).getRed();
					double p3 = reader.getColor(ogX,ogY + 1).getRed();
					double p4 = reader.getColor(ogX +1,ogY +1).getRed();

					double dy = y - ogY;
					double dx = x - ogX;

					double topLine = linearInterpolation(p1, p2, dx);
					double bottomLine = linearInterpolation(p3, p4, dx);
					double colour = linearInterpolation(topLine, bottomLine, dy);

					//colouring new pixel
					writer.setColor(i,j,Color.color(colour,colour,colour,1));
				}
			}
		}
		return newImage;
	}

	public double linearInterpolation(double firstP, double secondP, double deltaVal){
		return ((firstP * (1 - deltaVal)) + (secondP * deltaVal));
	}


	 /**
	  * Function to calculate the MIP if light was passed through top of the skull down to the bottom (Chin)
	  *
	  * These function shows how to carry out an operation on an image.
	  * It obtains the dimensions of the image, and then loops through
	  * the image carrying out the copying of a slice of data into the
	  * image.
	  */

	public void MIPTop2Bottom(WritableImage image, int z, boolean isMIP) {
		//Get image dimensions, and declare loop variables
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {

				if (isMIP == true) {

					int maxIntensity = min; //initializes the maximum intensity to the min value

					for (k = 0; k < cthead.length; k++) { // k goes through all the z slices
						maxIntensity = Math.max(cthead[k][j][i], maxIntensity); //sets maxIntensity to the max of current slice and current max
					}
					col = (((float) maxIntensity - (float) min) / ((float) (max - min)));
					for (c = 0; c < 3; c++) {
						image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
					}

				} else {
					//get values from slice according to slide number
					datum = cthead[z][j][i];
					//calculate the colour by performing a mapping from [min,max] -> [0,255]
					col = (((float) datum - (float) min) / ((float) (max - min)));
					for (c = 0; c < 3; c++) {
						image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

					} // colour loop
				} // loop for else condition of isMIP
			} // column loop
		}// row loop
	}


	public void MIPFront2Back(WritableImage image, int y, boolean isMIP) {
		//Get image dimensions, and declare loop variables
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		//Shows how to loop through each pixel and colour
		for (k = 0; k < h; k++) {
			for (i = 0; i < w; i++) {

				if (isMIP == true) {
					int maxIntensity = min;

					for (j = 0; j < 256; j++) {
						maxIntensity = Math.max(cthead[k][j][i], maxIntensity);
					}
					col = (((float) maxIntensity - (float) min) / ((float) (max - min)));
					for (c = 0; c < 3; c++) {
						image_writer.setColor(i, k, Color.color(col, col, col, 1.0));
					}

				} else {

					//get values from slice according to slide number
					datum = cthead[k][y][i];
					//calculate the colour by performing a mapping from [min,max] -> [0,255]
					col = (((float) datum - (float) min) / ((float) (max - min)));
					for (c = 0; c < 3; c++) {
						//and now we are looping through the bgr components of the pixel to
						//set the colour component c of pixel (i,j)
						image_writer.setColor(i, k, Color.color(col, col, col, 1.0));

					} // colour loop
				}
			} // column loop
		}// row loop
	}


	public void MIPSide2Side(WritableImage image, int x, boolean isMIP) {
		//Get image dimensions, and declare loop variables
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;

		for (k = 0; k < h; k++) {
			for (j = 0; j < w; j++) {

				if (isMIP == true) {

					int maxIntensity = min;

					for (i = 0; i < 256; i++) {
						maxIntensity = Math.max(cthead[k][j][i], maxIntensity);
					}
					col = (((float) maxIntensity - (float) min) / ((float) (max - min)));
					for (c = 0; c < 3; c++) {
						image_writer.setColor(j, k, Color.color(col, col, col, 1.0));
					} // colour loop


				} else {

					//get values from slice according to slide number
					datum = cthead[k][j][x];
					//calculate the colour by performing a mapping from [min,max] -> [0,255]
					col = (((float) datum - (float) min) / ((float) (max - min)));
					for (c = 0; c < 3; c++) {
						image_writer.setColor(j, k, Color.color(col, col, col, 1.0));
					} // colour loop
				}
			} // column loop
		}// row loop
	}


	public static void main(String[] args) {
		launch();
	}
}