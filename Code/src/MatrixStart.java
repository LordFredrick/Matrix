import java.util.Scanner;

public class MatrixStart {
	static Scanner in = new Scanner(System.in);

	public static void main(String[] args)
    {
		System.out.println("Pick The Format For the Effect:\n1-Full Screen Mode\n2-Windowed Mode");
		while (true) {
			int response = in.nextInt();
			if (response == 1) {
				System.out.println("Full Screen Mode Selected");
				int fps = 80;
				double period = 12.5;
				System.out.println("fps: " + fps + "; period: " + period + " ms");
				new MatrixCode("Matrix Rain", (long) (period * 1000000L), "full");
			}
			if (response == 2) {
				System.out.println("Windowed Mode Selected\nHow many Windows Would You Like?");
				int windowNumber = in.nextInt();
				int i = 0;
				int fps = 80;
				double period = 12.5;
				System.out.println("fps: " + fps + "; period: " + period + " ms");
				while(i < windowNumber){
					new MatrixCode("Matrix Rain", (long) (period * 1000000L), "not");
					i++;
				}
			} else
				System.out.println("That Was Not a Option Imbecele!\nTry Again");

		}
	}
}