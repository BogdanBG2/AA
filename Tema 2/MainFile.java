import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class MainFile {
	public static void main(String[] args) throws FileNotFoundException {
		Scanner input = new Scanner(new File("graph.in"));
		Graph g = new Graph();
		g.readGraph(input);
		input.close();
		g.setXA();
		
		PrintWriter output = new PrintWriter(new File("bexpr.out"));
		if(!g.isValidGraph()) {
			g.printInvalidGraph(output);
			output.close();
			return;
		}
		String bool_expr = g.iterateBy_X();
		bool_expr = g.andOperation(bool_expr, g.generateValidEdges());
		bool_expr = g.andOperation(bool_expr, g.iterateBy_A());

		output.print(bool_expr);
		output.close();
	}
}
