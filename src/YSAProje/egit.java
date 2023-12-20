package YSAProje;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class egit {
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		YSA ysa = null;
		Scanner in = new Scanner(System.in);
		int araKatmanNoronSayisi;
		double momentum,ogrenmeKatsayisi,maxError;
		int epoch,sec;
		do{
			System.out.println("1.Egitim ve test");
			System.out.println("2. Tek satir test");
			System.out.println("3. cikis");
			sec = in.nextInt();
			switch(sec)
			{
				case 1:
					/*System.out.println("arakatman noron sayisi");
					araKatmanNoronSayisi = in.nextInt();

					System.out.println("momentum");
					momentum = in.nextDouble();

					System.out.println("ogrenme katsaiyisi");
					ogrenmeKatsayisi = in.nextDouble();
					
					System.out.println("max hata");
					maxError = in.nextDouble();
					
					System.out.println("epoch");
					epoch = in.nextInt();*/
					
					//ysa = new Ysa(araKatmanNoronSayisi,momentum,ogrenmeKatsayisi,maxError,epoch);
					ysa = new YSA(20,0.4,0.3,0.0001,1000);
					
					ysa.Egit();
					System.out.println("egitimdeki hata: "+ysa.egitimHata());
					System.out.println("test hata: "+ysa.test());
					
					break;
				case 2 :
					if(ysa!=null)
					{
						double[] inputs = new double[2];
						System.out.println("frenBasinci:"); 
						
						inputs[0] = in.nextDouble();

						System.out.println("surtunme Katsayisi:");

						inputs[1] = in.nextDouble();

						
						
						String cikti = ysa.tekSatirTest(inputs);
						System.out.println("Cikti: "+cikti);
					}
					break;
			
			}
			
		}while(sec!=3);
	}
	public static double[] ulkeSayisal(String ulke)
	{
		double[] sayisal = new double[2];
		sayisal[0]=0;sayisal[1]=0;
		if(ulke.equals("Asia")) sayisal[1] = 1;
		if(ulke.equals("Europe")) sayisal[0] = 1;
		return sayisal;
	}
}