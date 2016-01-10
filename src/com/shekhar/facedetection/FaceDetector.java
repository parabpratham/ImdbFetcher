package com.shekhar.facedetection;

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

import com.imdb.fetcher.DownloadPage;

public class FaceDetector {

	private CascadeClassifier faceDetector;

	public FaceDetector() {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		faceDetector = new CascadeClassifier(FaceDetector.class.getResource(
				"haarcascade_frontalface_alt.xml").getPath());
	}

	public static void main(String[] args) {

		FaceDetector detector = new FaceDetector();
		Mat image = detector.fetchFaces("ouput_face_0.png");
		String filename = "ouput.png";
		System.out.println("Writing " + filename);
		Highgui.imwrite(filename, image);
	}

	private Mat fetchFaces(String filePath) {
		Mat image = Highgui.imread(FaceDetector.class.getResource(filePath)
				.getPath());

		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		System.out.println("Detected " + faceDetections.toArray().length
				+ " faces");
		Rect[] array = faceDetections.toArray();

		int count = 0;
		for (Rect rect : array) {

			String filename = "ouput_face_" + count++ + ".png";
			Highgui.imwrite(filename, image);
			cropImage(new File(filename), rect);
			Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x
					+ rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		}

		return image;
	}

	public void fetchFaces(String fileName, String filePath) {
		Mat image = Highgui.imread(FaceDetector.class.getResource(filePath)
				.getPath());

		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		System.out.println("Detected " + faceDetections.toArray().length
				+ " faces");
		Rect[] array = faceDetections.toArray();

		int count = 0;
		for (Rect rect : array) {
			String filename = DownloadPage.storagePath + "faces/" + fileName
					+ "_" + count++ + ".png";
			Highgui.imwrite(filename, image);
			cropImage(new File(filename), rect);
			Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x
					+ rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		}
	}

	private static void cropImage(File img, Rect rect) {
		BufferedImage src;
		try {

			src = ImageIO.read(img);
			BufferedImage dest = src.getSubimage(rect.x, rect.y, rect.width,
					rect.height);
			String filename = "ouput_face.png";
			System.out.println("Writing " + filename);
			ImageIO.write(dest, "png", img);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}