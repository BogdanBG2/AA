import java.util.Scanner;
import java.io.PrintWriter;

public class Graph {

	int no_nodes; // numarul de noduri = N
	int[][] edges; // matricea de adiacenta
	String[][] x;
	String[][] a;

	// Citeste informatiile despre graf cu un Scanner dat ca parametru.
	public void readGraph(Scanner in) {
		no_nodes = in.nextInt();
		edges = new int[no_nodes][no_nodes];
		String buffer = in.nextLine();
		String [] ends;
		
		while(in.hasNextLine()) {
			buffer = in.nextLine();
			if(buffer.equals("-1"))
				break;
			ends = buffer.split(" ");
			int i = Integer.parseInt(ends[0]);
			int j = Integer.parseInt(ends[1]);
			edges[i-1][j-1] = 1;
			edges[j-1][i-1] = 1;
		}
	}
	
	// Initializeaza toate variabilele de tip "x i-j" si "a i-j" posibile.
	public void setXA() {
		int n = no_nodes;
		x = new String[n][n];
		a = new String[n/2+1][n];
		for(int i = 1; i <= n; ++i)
			for(int j = 1; j <= n; ++j)
				x[i-1][j-1] = "x" + i + "-" + j;
		
		for(int i = 1; i <= n / 2 + 1; ++i)
			for(int j = 1; j <= n; ++j)
				a[i-1][j-1] = "a" + i + "-" + j;
	} // Complexitate totala: O(N^2)

	// Operatiile folosite; Complexitatea totala a fiecarei operatii: O(1)
	public String negateOperation(String op) {
		return "~" + op;
	}
	public String andOperation(String op1, String op2) {
		return op1 + "&" + op2;
	}
	public String orOperation(String op1, String op2) {
		return op1 + "|" + op2;
	}
	public String addBrackets(String op) {
		return "(" + op + ")";
	}
	public String composedExpresion(String op1, String op2) { // (x|~y)&(~x|y)
		String part1 = this.orOperation(op1, this.negateOperation(op2));
		part1 = this.addBrackets(part1);
		
		String part2 = this.orOperation(this.negateOperation(op1), op2);
		part2 = this.addBrackets(part2);
		
		String result = this.andOperation(part1, part2);
		return this.addBrackets(result);
	}
	
	// Cate noduri pleaca din nodul idx?
	public int edgesFromNode(int idx) {
		int i = idx - 1;
		int result = 0;
		for(int e : edges[i])
			result += e;
		return result;
	} // Complexitate totala: O(N)
	
	// Graful nostru este unul valid acestei cerinte?
	public boolean isValidGraph() {
		for(int idx = 1; idx <= no_nodes; ++idx)
			if(this.edgesFromNode(idx) < 2)
				return false;
		return true;
	} // Complexitate totala: O(N)
	
	// Ce se intampla in cazul unui graf invalid?
	public void printInvalidGraph(PrintWriter output) {
		output.print("x1-1 & ~x1-1"); // expresie mereu egala cu 0
	} // Complexitate totala: O(1)
	
	/*
	 	Genereaza expresiile de forma "a 1-i|a 2-i|...|a lng-i",
	 	care se traduc astfel : Este lungimea de la 1 la "i", pe drumul ales, cel mult "lng"?
	*/
	public String generate_A_Values(int i, int lng) { // parametrii: nod, lungime maxima
		String result = a[0][i];
		for(int j = 1; j < lng; ++j) // cel mult O(N/2 + 1) = O(N)
			result = this.orOperation(result, this.a[j][i]);
		result = this.addBrackets(result);
		return result;
	} // Complexitate totala: O(N)
	
	/*
	 	Genereaza o expresie booleana pentru nodul "node",
	 	prin care verificam daca ciclul hamiltonian ales 
	 	are exact 2 laturi care trec prin nodul dat ca parametru;
	 	in cazul in care nu studiem nodul 1, se verifica si daca
	 	cel mai scurt drum de la 1 la "node", pe drumul ales,
	 	este de lungime cel mult (N/2 + 1).
	*/
	public String generateEdgeExpressionForPath(int node) {
		String result = "";
		
		int i = node - 1;
		for(int j = 0; j < no_nodes; ++j) { // O(N)
			if(edges[i][j] == 0) // se neglizeaza drumurile care nu exista
				continue;
			String q = "";
			
			// adaugarea lui x-i-j in ecuatie
			for(int k = j + 1; k < no_nodes; ++k) { // O(N)
				if(edges[i][k] == 0) // O(N); se exclude conditia k == j, mereu falsa
					continue; // nu avem muchie => se neglijeaza pozitia curenta
				
				q = this.andOperation(x[i][j], x[i][k]); // laturile care sigur sunt in ciclu
				for(int idx = 0; idx < no_nodes; ++idx) // O(N)
					if(idx != i && idx != j && idx != k && edges[i][idx] == 1)
						q = this.andOperation(q, this.negateOperation(x[i][idx]));
				q = this.addBrackets(q);
				
				if(result.equals(""))
					result = q; // initializare cu primul termen
				else
					result = this.orOperation(result, q);
			} // O(N^2)
			
		result = this.addBrackets(result);
		} // O(N^3)
		
		// partea de final, daca nu analizam primul nod
		if(i != 0) {
			String aux = this.generate_A_Values(i, no_nodes/2 + 1); // O(N)
			result = this.andOperation(result, aux);
		}
				
		return result;
	} // Complexitate totala: O(N^3)
	
	// Adauga toate expresiile generate de functia descrisa mai sus.
	public String iterateBy_X() {
		String result = this.generateEdgeExpressionForPath(1);
		for(int i = 2; i <= no_nodes; ++i) // O(N)
			result = this.andOperation(result, this.generateEdgeExpressionForPath(i)); 
		return result;
	} // Complexitate totala: O(N^3) * O(N) = O(N^4)
	
	/*
	 	Genereaza o expresie booleana care verifica daca fiecare muchie din graf este luata
		cel mult o data.
	*/
	public String generateValidEdges() {
		String result = "";
		int first = 1;
		for(int i = 0; i < no_nodes; ++i) // O(N)
			for(int j = i+1; j < no_nodes; ++j) // O(N)
				if(edges[i][j] == 1) {
					String component = this.composedExpresion(this.x[i][j], this.x[j][i]);
					if(first == 1) {
						result = component;
						first = 0;
					}
					else
						result = this.andOperation(result, component);
				}
		return result;
	} // Complexitate totala: O(N^2)
	
	// Genereaza expresiile de forma "a 1-p".
	public String firstA() {
		String result = "";
		for(int i = 1; i < no_nodes; ++i) // O(N)
			if(edges[0][i] == 1)
				if(result.equals(""))
					result = this.composedExpresion(a[0][i], x[0][i]);
				else {
					String aux = this.composedExpresion(a[0][i], x[0][i]);
					result = this.andOperation(result, aux);
				}
		
		for(int i = 0; i < no_nodes; ++i) // O(N)
			if(edges[0][i] == 0)
				result = this.andOperation(result, this.negateOperation(a[0][i]));
		return result;
	}
	
	// Genereaza expresia corespunzatoare lui "a len-idx".
	public String A_Iteration(int len, int idx) {
		int L = len - 1; // indicele de linie din matricea "a"
		int i = idx - 1; // indicele de coloana din matricea "a"
		String result = "";
		String s1 = a[L][i];
		String s2 = "";
		
		for(int j = 1; j < no_nodes; ++j) // O(N)
			if(edges[i][j] == 1) { // daca e muchie valida ->
				// se adauga termenul de forma de mai jos
				String aux = this.addBrackets(this.andOperation(a[L-1][j], x[j][i]));
				if(s2.equals(""))
					s2 = aux;
				else
					s2 = this.orOperation(s2, aux);
			}
		s2 = this.addBrackets(s2);
		
		String s3 = this.generate_A_Values(i, L); // O(N)
		s2 = this.andOperation(s2, this.negateOperation(s3));
		s2 = this.addBrackets(s2);
		
		result = this.composedExpresion(s1, s2);
		return result;
	} // Complexitate totala: O(N)
	
	// Genereaza toate expresiile corespunzatoare temenilor de tip "a i-j"
	public String iterateBy_A() {
		String result = this.firstA();
		for(int i = 2; i <= no_nodes/2 + 1; ++i) // O(N)
			for(int j = 2; j <= no_nodes; ++j) // O(N)
				result = this.andOperation(result, this.A_Iteration(i, j)); // O(N)
		return result;
	} // Complexitate totala: O(N^3)
}
