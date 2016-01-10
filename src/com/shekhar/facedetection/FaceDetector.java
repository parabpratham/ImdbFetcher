package com.shekhar.facedetection;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {

	private CascadeClassifier faceDetector;

	private final int IMG_WIDTH = 125;
	private final int IMG_HEIGHT = 150;

	public static final String storagePath = "/home/pratham/workspace/ImdbFetcher/images/";

	public FaceDetector() {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		faceDetector = new CascadeClassifier(
				FaceDetector.class.getResource("haarcascade_frontalface_alt.xml").getPath());
	}

	public static void main(String[] args) {

		FaceDetector detector = new FaceDetector();

		File directory = new File(storagePath);
		String[] list = directory.list();
		for (String fileName : list) {
			try {
				Mat image = detector.fetchFaces(fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// String filename = "ouput.png";
		// System.out.println("Writing " + filename);
		// Highgui.imwrite(filename, image);
	}

	private Mat fetchFaces(String fileName) {
		Mat image = Highgui.imread(storagePath + "" + fileName);

		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		System.out.println(fileName + " Detected " + faceDetections.toArray().length + " faces");
		Rect[] array = faceDetections.toArray();

		int count = 0;
		for (Rect rect : array) {
			String opFileName = fileName + "_" + count++ + ".png";
			cropImage(new File(storagePath + fileName), rect, storagePath + "Faces/" + opFileName);
		}

		return image;
	}

	private void cropImage(File inImg, Rect rect, String outputFileName) {
		BufferedImage src;
		try {

			src = ImageIO.read(inImg);
			BufferedImage dest = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
			System.out.println("Writing " + outputFileName);
			ImageIO.write(dest, "png", new File(outputFileName));

			int type = src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType();

			BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);

			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(src, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
			g.dispose();
			g.setComposite(AlphaComposite.Src);

			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			ImageIO.write(dest, "png", new File(outputFileName + "_125_150"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}