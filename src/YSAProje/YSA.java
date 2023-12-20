package YSAProje;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;

public class YSA {
	private static final File egitimDosya = new File(YSA.class.getResource("Data.txt").getPath());
	private static final File testDosya = new File(YSA.class.getResource("DataTest.txt").getPath());

	private DataSet egitimVeriSeti;
	private DataSet testVeriSeti;

	private double[] minumumlar;
	private double[] maksimumlar;

	private int araKatmanNoron;
	private MomentumBackpropagation mbp;

	public YSA(int araKatmanNoron, double momentum, double ogrenmeKatsayisi, double maxHata, int epoch)
			throws FileNotFoundException {

		this.araKatmanNoron = araKatmanNoron;

		minumumlar = new double[3];
		maksimumlar = new double[3];
		for (int i = 0; i < 1; i++) {
			minumumlar[i] = Double.MAX_VALUE;
			maksimumlar[i] = Double.MIN_VALUE;
		}

		// Eðitim ve test veri setlerindeki minimum ve maksimum deðerleri bul
		minimumVeMaksimumlarBul(egitimDosya);
		minimumVeMaksimumlarBul(testDosya);

		egitimVeriSeti = veriSetiOku(egitimDosya);
		testVeriSeti = veriSetiOku(testDosya);

		mbp = new MomentumBackpropagation();
		mbp.setLearningRate(ogrenmeKatsayisi);
		mbp.setMaxError(maxHata);
		mbp.setMaxIterations(epoch);
		mbp.setMomentum(momentum);
	}

	public void Egit() {
		MultiLayerPerceptron sinirselAg = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 2, araKatmanNoron, 1);
		System.out.println(egitimVeriSeti);
		sinirselAg.setLearningRule(mbp);

		sinirselAg.learn(egitimVeriSeti);

		System.out.println("Egitim Tamamlandi");
		sinirselAg.save("model.nnet");
	}

	// Test iþlemini gerçekleþtir ve ortalama hata deðerini döndür
	public double test() {
		// Modeli dosyadan yükle
		NeuralNetwork sinirselAg = NeuralNetwork.createFromFile("model.nnet");

		// Toplam hata deðeri
		double toplamHata = 0;

		// Test veri setindeki her bir satýr üzerinde iþlem yap
		for (var satir : testVeriSeti.getRows()) {
			// Modelin girdisini belirle
			sinirselAg.setInput(satir.getInput());

			// Modelin çýktýsýný hesapla
			sinirselAg.calculate();

			// Toplam hata deðerine bu satýrdaki hata eklenir
			toplamHata += mse(satir.getDesiredOutput(), sinirselAg.getOutput());
		}

		// Ortalama hata deðerini döndür
		return toplamHata / testVeriSeti.size();
	}

	// Eðitim iþleminin gerçekleþtiði aþamada toplam hata deðerini döndür
	public double egitimHata() {
		return mbp.getTotalNetworkError();
	}

	// Belirtilen giriþ deðerleri üzerinde tek bir test iþlemi gerçekleþtir ve
	// sonucu döndür
	public String tekSatirTest(double[] inputs) {
		// Giriþ deðerlerini normalleþtir
		double[] inputD = new double[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			inputD[i] = minMax(inputs[i], minumumlar[i], maksimumlar[i]);
		}

		// Modeli dosyadan yükle
		NeuralNetwork sinirselAg = NeuralNetwork.createFromFile("model.nnet");

		// Modelin girdisini belirle
		sinirselAg.setInput(inputD);

		// Modelin çýktýsýný hesapla
		sinirselAg.calculate();

		// Gerçek çýktýyý döndür
		return gercekCikti(sinirselAg.getOutput());
	}

	// Modelin çýktýsýna göre gerçek çýktýyý belirle
	private String gercekCikti(double[] output) {

		return String.valueOf((int)(output[0]*40))+" Fren/saniye";
//		double max = Double.MIN_VALUE;
//		int maxIndex = 0;
//
//		// Çýkýþ deðerleri arasýnda en büyük olaný bul
//		for (int i = 0; i < output.length; i++) {
//			if (output[i] > max) {
//				max = output[i];
//				maxIndex = i;
//			}
//		}
//
//		// En büyük olan deðere göre etiketi belirle ve döndür
//		switch (maxIndex) {
//		case 0:
//			return "kötü";
//		case 1:
//			return "normal";
//		case 2:
//			return "iyi";
//		default:
//			return "hata";
//		}
	}

	// Ortalama karesel hata (MSE) hesapla
	private double mse(double[] beklenen, double[] cikti) {
		double birSatirHata = 0;
		// Her bir çýkýþ için hata hesapla
		for (int i = 0; i < beklenen.length; i++) {
			birSatirHata += Math.pow(beklenen[i] - cikti[i], 2);
		}
		// Toplam hatayý döndür
		return birSatirHata / 3;
	}

	// Dosyadan veri setini oku
	private DataSet veriSetiOku(File dosya) throws FileNotFoundException {
		Scanner in = new Scanner(dosya);

		// DataSet nesnesi oluþtur, 2 giriþ sayýsý, 1 çýkýþ sayýsý
		DataSet ds = new DataSet(2, 1);

		// Dosya sonuna kadar her bir satýrý oku
		while (in.hasNextLine()) {

			String[] values = in.nextLine().split("\\s+"); // Satirdaki deðerleri boþluklara göre ayýr
			double[] input = new double[2];// Giriþ deðerlerini oku ve normalleþtir
			double[] output = new double[1];
			for (int i = 0; i < 3; i++) {
				double d = Double.parseDouble(values[i]);
				// Giriþ deðerlerini normalleþtir
				if (i == 2) {
					output[0] = minMax(d, minumumlar[i], maksimumlar[i]);

				} else {
					input[i] = minMax(d, minumumlar[i], maksimumlar[i]);
				}
			}

			// Çýkýþ deðerlerini oku

			// Satýrý DataSet'e ekle
			DataSetRow satir = new DataSetRow(input, output);
			ds.add(satir);
		}

		// DataSet'i döndür
		return ds;
	}

	// Deðerin min-max normalizasyonunu yap
	private double minMax(double d, double min, double max) {
		return (d - min) / (max - min);
	}

	// Dosyadaki minimum ve maksimum deðerleri bul
	private void minimumVeMaksimumlarBul(File dosya) throws FileNotFoundException {
		Scanner in = new Scanner(dosya);
		// Dosya sonuna kadar her bir satýrý oku
		while (in.hasNextLine()) {

			String[] values = in.nextLine().split("\\s+"); // Satirdaki deðerleri boþluklara göre ayýr
			// Her bir giriþ deðeri için minimum ve maksimumu güncelle
			for (int i = 0; i < 3; i++) {
				double d = Double.parseDouble(values[i]);
				if (d < minumumlar[i])
					minumumlar[i] = d;
				if (d > maksimumlar[i])
					maksimumlar[i] = d;
			}
			// Çýkýþ deðerlerini oku, ancak kullanýlmayacak

		}
	}
}
