import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class genetico {
	static FileWriter arq;
	static PrintWriter gravarArq;
	static final int fgold = 0;
	static final int fgold2 = 1;
	static final int fbump1 = 2;
	static final int frastrigin = 3;
	static final int cross1p = 0;
	static final int cross2p = 1;
	static final int mutSimples = 0;
	static final int mutNaoUniforme = 1;
	static final int selitismo = 0;
	static final int nelitismo = 1;
	static final int trocaPopGer = 0;
	static final int trocaPopFixo = 1;
	static final int condParadaGer = 0;
	static final int condParadaEstag = 1;
	static int quantSelecElitismo;
	static int tamCromossomo;
	static int tamPopulacao;
	static int funcaoOtimizada;
	static int crossover;
	static int mutacao;
	static int elitismo;
	static int trocaPopulacao;
	static int condicaoParada;
	static int inf;
	static int sup;
	static int pontoCross1p;
	static int pontoCross2p;
	static int quantGerParada;
	static double porcMutacao;
	static int quantIndivGeracao;
	static int b;
	static double porcElitismo;
	static ArrayList<Double> listaFitnessMedio = new ArrayList<Double>();
		
	public static ArrayList<Individuo> funSElitismo(ArrayList<Individuo> populacao, ArrayList<Individuo> novaPopulacao){
		//Mantem a quantidade definidia pelo parametro "porcElitismo" dos melhores individuos para a próxima geração
		for(int i = populacao.size()- (int)(tamPopulacao * porcElitismo);i<populacao.size();i++){
			novaPopulacao.add(populacao.get(i));
		}
		return novaPopulacao;//Retorna os melhores individuos
	}
	
	public static ArrayList<Individuo> funElitismo(ArrayList<Individuo> populacao, ArrayList<Individuo> novaPopulacao, int elitismo){
		//Define se o algoritmo irá para função com elitismos ou sem elitismo
		if(elitismo == selitismo){
			return funSElitismo(populacao, novaPopulacao);//Retorna o resultado obtido da função com elitismo
		}else{
			return novaPopulacao;//Não haverá elitismo
		}
	}
	
	/*
	 * Função de troca de população que une os 50 melhores indivíduos da atual com os 50 melhores da nova geração
	 */
	
	public static ArrayList<Individuo> funTrocaPopGer(ArrayList<Individuo> novaPopulacao, ArrayList<Individuo> populacao, int metodoTrocaPop){
		
		ArrayList<Individuo> populacaoNova2 = new  ArrayList<Individuo>();//Cria variável
		ArrayList<Individuo> populacao2 = new  ArrayList<Individuo>();//Cria variável
		populacaoNova2 = novaPopulacao;
		populacao2 = populacao;
		
		for(int i = 50;i<populacaoNova2.size();i++){
			populacaoNova2.remove(i);
		}
		for(int i = 50;i<populacao2.size();i++){
			populacao2.remove(i);
		}
		
		int j = 0;
		for(int i = 0;i<populacao2.size();i++){
			while(j<populacaoNova2.size() && populacao2.get(i).fitness<novaPopulacao.get(j).fitness){
				j++;
			}
			populacaoNova2.add(j, populacao2.get(i));
		}
		return populacaoNova2;
	}
	
	
	public static ArrayList<Individuo> funTrocaPop(ArrayList<Individuo> novaPopulacao, ArrayList<Individuo> populacao, int metodoTrocaPop){
		//Define se usará método de troca de população por geração
		if(metodoTrocaPop == trocaPopGer){
			return funTrocaPopGer(novaPopulacao, populacao, metodoTrocaPop);//Retorna população gerada pelo
			//método de troca de população por geração
		}else{
			return novaPopulacao; //Populacao nova será apenas com filhos
		}
	}
	
	/*
	 * A partir de uma probabilidade definida pelo usuário, seleciona aleatoriamente
	 * um ponto no cromossomo do indivíduo selecionado também aleatoriamente e inverte o bit.
	 */
	public static ArrayList<Individuo> funMutacaoSimples(int funcao, int mutacao, ArrayList<Individuo> populacao, ArrayList<Individuo> novaPopulacao,int x){
		//Faz mutação simples de acordo com a probabilidade de acontecer mutação
		Individuo indiv = novaPopulacao.get(x);
		novaPopulacao.remove(x);//Remove o indivíduo selecionado para mutação da população atual
		if(Math.random()<=porcMutacao){//Gera numero randomico e compara com a probabilidade de haver mutação
			int randomPos = (int)(Math.random() * tamCromossomo);//Escolhe aleatoriamente a posição do cromossomo que será mutada
			//-----------------------------------------------------
			//Se a posição escolhida tiver valor 0 muta para 1 e vice versa
			//-----------------------------------------------------
			if(indiv.ind[randomPos] == 0){
				indiv.ind[randomPos] = 1;
			}else{
				indiv.ind[randomPos] = 0;
			}				
		//---------------------------------------------------------
			indiv.fitness = calculaFitness(funcao, indiv.ind);//Atualiza fitness após mutação
			}
		int j = 0;
		while(j<novaPopulacao.size() && indiv.fitness<novaPopulacao.get(j).fitness){
			j++;
		}
		novaPopulacao.add(j, indiv);//Insere o indivíduo mutado novamente na população
		return novaPopulacao;//Retorna população
	}
	
	/*
	 * É gerada uma probabilidade de mutação com base na geração atual. Se a geração for baixa(primeiras gerações),
	 * a probabilidade de mutação será alta. Se a geração for alta(muitas gerações já criadas), a probabilidade será baixa.
	 */
	public static ArrayList<Individuo> funMutacaoNaoUniforme(int funcao, int mutacao, ArrayList<Individuo> populacao, int contGer, ArrayList<Individuo> novaPopulacao,int x){
		//Método utilizado de acordo com o proposto por MICHALEWICZ (1996).
		double randomNum = Math.random();//Guarda um numero aleatório de ponto flutuante
		int randomPos = (int)(Math.random() * tamCromossomo);//Guarda um numero inteiro aleatório
		double r = 0;
		Individuo indiv = novaPopulacao.get(x);
		novaPopulacao.remove(x);//Remove o indivíduo selecionado para mutação da população atual
		r = Math.random();//Guarda um numero aleatório de ponto flutuante
		if(randomNum <= (1-Math.pow(r,Math.pow((1-contGer/quantGerParada),b)))){//De acordo com fórmula proposta por MICHALEWICZ
		//onde a cada nova geração a probabilidade de mutação diminui
			
			//-----------------------------------------------------
			//Se a posição escolhida tiver valor 0 muta para 1 e vice versa
			//-----------------------------------------------------
			if(indiv.ind[randomPos] == 0){
				indiv.ind[randomPos] = 1;
			}else{
				indiv.ind[randomPos] = 0;
			}		
			//-----------------------------------------------------

			indiv.fitness = calculaFitness(funcao, indiv.ind);//Atualiza o fitness da nova população
					
		}
		int j = 0;
		while(j<novaPopulacao.size() && indiv.fitness<novaPopulacao.get(j).fitness){
			j++;
		}
		novaPopulacao.add(j, indiv);//Insere o indivíduo mutado novamente na população
		return novaPopulacao;//Retorna nova população
	}
	
	public static ArrayList<Individuo> funMutacao(int funcao, int mutacao, ArrayList<Individuo> populacao, int contGer, ArrayList<Individuo> novaPopulacao,int x){
		//----------------------------------------------------------------
		//Define qual método de mutação será usado
		//----------------------------------------------------------------
		if(mutacao == mutSimples){
			return funMutacaoSimples(funcao, mutacao, populacao, novaPopulacao,x);
		}else{
			return funMutacaoNaoUniforme(funcao, mutacao, populacao, contGer, novaPopulacao,x);
		}
		//----------------------------------------------------------------
	}
	
	/*
	 * Esta seleção por roleta utiliza uma técnica específica:
	 * A função gold2 é a única em que o melhor fitness é um número baixo, ou seja
	 * dificilmente este número será escolhido, já que o melhor fitness se aproxima de 0.
	 * Portanto, é criada uma lista temporaria de individuos iguais à populacao original.
	 * Contudo o valor de fitness é subtraído pelo máximo do domínio da função: em torno de 11*(10^5)
	 * gerando valores fitness que representem a ideia de que: quanto maior este valor, melhor o fitness do individuo,
	 * tornando possível a utilização da roleta
	 */
	
	public static Individuo selecaoRoleta(ArrayList<Individuo> populacao){
		Individuo indiv = new Individuo(tamCromossomo);//Cria um novo individuo
		if(funcaoOtimizada == fgold2){
			ArrayList<Individuo> populacaoTemp = populacao;
			if(funcaoOtimizada == fgold2){
				for(int i = 0;i<populacaoTemp.size();i++){
					populacaoTemp.get(i).fitness = 11*Math.pow(10, 5) - populacaoTemp.get(i).fitness;
				}
				Individuo indivReinserir;
				for(int i = 0;i<populacaoTemp.size();i++){
					indivReinserir = populacaoTemp.get(i);
					populacaoTemp.remove(i);
					int j = 0;
					while(j<populacaoTemp.size() && indivReinserir.fitness<populacaoTemp.get(j).fitness){
						j++;
					}
					populacaoTemp.add(j, indivReinserir);
				}
			}
			double totalFitness = 0;
			totalFitness = 0;
			//------------------------------------------------------
			//Calcula soma negativo do fitness da população
			//------------------------------------------------------
			for(int i =0;i<populacaoTemp.size();i++){
				totalFitness -= Math.abs(populacaoTemp.get(i).fitness);
			}
			//------------------------------------------------------
			//totalFitness = totalFitness;
			double randomNum;
			double fitnessCalc = 0;
			//------------------------------------------------------
			//Gera um número aleatório de 0 até o somatório negativo do fitness da função
			//------------------------------------------------------
			randomNum = (double) (totalFitness * Math.random()); 
			//Este for basicamente irá subtrair o fitness de cada indivíduo pelo anterior
			//até atingir um valor que seja menor ou igual ao valor aleatório de randomNum
			for(int i = 0;i<populacaoTemp.size();i++){
				fitnessCalc -= Math.abs(populacaoTemp.get(i).fitness);		
				if(fitnessCalc <= randomNum){	
					indiv = populacaoTemp.get(i);
					indiv.fitness = calculaFitness(funcaoOtimizada, indiv.ind);
					break;
				}
			}	
			
		}else{	
		double totalFitness = 0;
		totalFitness = 0;
		//------------------------------------------------------
		//Calcula soma negativo do fitness da população
		//------------------------------------------------------
		for(int i =0;i<populacao.size();i++){
			totalFitness -= Math.abs(populacao.get(i).fitness);
		}
		//------------------------------------------------------
		//totalFitness = totalFitness;
		double randomNum;
		double fitnessCalc = 0;
		//------------------------------------------------------
		//Gera um número aleatório de 0 até o somatório negativo do fitness da função
		//------------------------------------------------------
		randomNum = (double) (totalFitness * Math.random()); 
		//Este for basicamente irá subtrair o fitness de cada indivíduo pelo anterior
		//até atingir um valor que seja menor ou igual ao valor aleatório de randomNum
		for(int i = 0;i<populacao.size();i++){
			fitnessCalc -= Math.abs(populacao.get(i).fitness);		
			if(fitnessCalc <= randomNum){	
				indiv = populacao.get(i);
				break;
			}
		}	
		}
		//------------------------------------------------------
		return indiv; //Retorna o individuo selecionado pela roleta
	}
	
	/*
	 * Função de Crossover de um ponto, que divide o cromossomo de dois indivíduos em um ponto aleatório e 
	 * inverte as partes divididas, gerando dois novos indivíduos.
	 */
	public static ArrayList<Individuo> funCrossover1p(int funcao, int crossover, ArrayList<Individuo> populacao, ArrayList<Individuo> novaPopulacao){
		Individuo[] IndivSelecionadosCross = new Individuo[2];//Cria array de individuos
		Individuo novoIndiv1 = new Individuo(tamCromossomo);//Cria individuo
		Individuo novoIndiv2 = new Individuo(tamCromossomo);//Cria individuo
		IndivSelecionadosCross[0] = selecaoRoleta(populacao);//Seleciona individuo pelo método da roleta
		IndivSelecionadosCross[1] = selecaoRoleta(populacao);//Seleciona individuo pelo método da roleta
		
		//-----------------------------------------------------------------------------
		//Se os dois individuos selecionados forem o mesmo, fica no looping trocando um individuo
		//selecionado até que os dois sejam diferentes
		//-----------------------------------------------------------------------------
		while(IndivSelecionadosCross[0]==IndivSelecionadosCross[1]){
			IndivSelecionadosCross[1] = selecaoRoleta(populacao);
		}
		//-----------------------------------------------------------------------------
				int randomPos = 1 + (int)(Math.random() * tamCromossomo-1); 
		//-----------------------------------------------------------------------------
		//Copia a primeira parte do individuo0 e a segunda parte do individuo1 para o novo individuo
		//-----------------------------------------------------------------------------
				for(int a = 0;a<randomPos;a++){
					novoIndiv1.ind[a] = IndivSelecionadosCross[0].ind[a];
				}
				for(int a = randomPos;a<novoIndiv1.ind.length;a++){
					novoIndiv1.ind[a] = IndivSelecionadosCross[1].ind[a];
				}
		//-----------------------------------------------------------------------------
				
		//-----------------------------------------------------------------------------
		//Copia a primeira parte do individuo1 e a segunda parte do individuo0 para o novo individuo
		//-----------------------------------------------------------------------------
				for(int a = 0;a<randomPos;a++){
					novoIndiv2.ind[a] = IndivSelecionadosCross[1].ind[a];
				}
				for(int a = randomPos;a<novoIndiv2.ind.length;a++){
					novoIndiv2.ind[a] = IndivSelecionadosCross[0].ind[a];
				}
		//-----------------------------------------------------------------------------

				//Calcula fitness dos novos individuos
				novoIndiv1.fitness = calculaFitness(funcao, novoIndiv1.ind);
				novoIndiv2.fitness = calculaFitness(funcao, novoIndiv2.ind);
				//---------------------------------------------------------------------
				int j = 0;
				
				//Insere os individuos no lugar ordenado no array novaPopulacao
				while(j<novaPopulacao.size() && novoIndiv1.fitness<novaPopulacao.get(j).fitness){
					j++;
				}
				novaPopulacao.add(j, novoIndiv1);
				j = 0;
				while(j<novaPopulacao.size() && novoIndiv2.fitness<novaPopulacao.get(j).fitness){
					j++;
				}
				novaPopulacao.add(j, novoIndiv2);
				//----------------------------------------------------------------------
		return novaPopulacao;//Retorna nova população
	}
	
	/*
	 * Função crossover de dois pontos, seleciona dois indivíduos através da roleta e gera divide o cromossomo destes
	 * em três partes, com pontos de partição aleatórios. Estas partes são intercaladas entre si e são gerados dois novos indivíduos.
	 */
	public static ArrayList<Individuo> funCrossover2p(int funcao, int crossover, ArrayList<Individuo> populacao, ArrayList<Individuo> novaPopulacao){
		Individuo[] IndivSelecionadosCross = new Individuo[2];//Cria array de individuos
		Individuo novoIndiv1 = new Individuo(tamCromossomo);//Cria individuo
		Individuo novoIndiv2 = new Individuo(tamCromossomo);//Cria individuo
		IndivSelecionadosCross[0] = selecaoRoleta(populacao);//Seleciona individuo pelo método da roleta
		IndivSelecionadosCross[1] = selecaoRoleta(populacao);//Seleciona individuo pelo método da roleta
		
		//-----------------------------------------------------------------------------
		//Se os dois individuos selecionados forem o mesmo, fica no looping trocando um individuo
		//selecionado até que os dois sejam diferentes
		//-----------------------------------------------------------------------------
		while(IndivSelecionadosCross[0]==IndivSelecionadosCross[1]){
			IndivSelecionadosCross[1] = selecaoRoleta(populacao);
		}
		//-----------------------------------------------------------------------------
		
		int randomPos1 = 1 + (int)(Math.random() * (tamCromossomo-1));//Escolhe a posição do primeiro ponto de corssover
		int randomPos2 = 0 + (int)(Math.random() * (tamCromossomo-randomPos1));//Escolhe a posição do segundo ponto de crossover
			
				//-----------------------------------------------------------------------------
				//Copia a primeira parte do individuo0 e a segunda parte do individuo1 e a terceira parte do individuo0
				//para o novo individuo
				//-----------------------------------------------------------------------------
				for(int a = 0;a<randomPos1;a++){
					novoIndiv1.ind[a] = IndivSelecionadosCross[0].ind[a];
				}
				for(int a = randomPos1;a<randomPos2;a++){
					novoIndiv1.ind[a] = IndivSelecionadosCross[1].ind[a];
				}
				for(int a = randomPos2;a<novoIndiv1.ind.length;a++){
					novoIndiv1.ind[a] = IndivSelecionadosCross[0].ind[a];
				}
				//------------------------------------------------------------------------------
				
				//------------------------------------------------------------------------------
				//Copia a primeira parte do individuo1 e a segunda parte do individuo0 e a terceira parte do individuo1
				//para o novo individuo
				//------------------------------------------------------------------------------			
				for(int a = 0;a<randomPos1;a++){
					novoIndiv2.ind[a] = IndivSelecionadosCross[1].ind[a];
				}
				for(int a = randomPos1;a<randomPos2;a++){
					novoIndiv2.ind[a] = IndivSelecionadosCross[0].ind[a];
				}
				for(int a = randomPos2;a<novoIndiv2.ind.length;a++){
					novoIndiv2.ind[a] = IndivSelecionadosCross[1].ind[a];
				}
				//-----------------------------------------------------------------------------
				
				//Calcula fitness dos novos individuos
				novoIndiv1.fitness = calculaFitness(funcao, novoIndiv1.ind);
				novoIndiv2.fitness = calculaFitness(funcao, novoIndiv2.ind);
				//-----------------------------------------------------------------------------
				int j = 0;
				
				//Insere os individuos no lugar ordenado no array novaPopulacao
				while(j<novaPopulacao.size() && novoIndiv1.fitness<novaPopulacao.get(j).fitness){
					j++;
				}
				novaPopulacao.add(j, novoIndiv1);
				j = 0;
				while(j<novaPopulacao.size() && novoIndiv2.fitness<novaPopulacao.get(j).fitness){
					j++;
				}
				novaPopulacao.add(j, novoIndiv2);
				//-----------------------------------------------------------------------------
		return novaPopulacao;//Retorna a nova população
	}
	
	public static ArrayList<Individuo> funCrossover(int funcao, int crossover, ArrayList<Individuo> populacao, ArrayList<Individuo> novaPopulacao){
		//Escolhe qual método de crossover será invocado
		if(crossover == cross1p){
			return(funCrossover1p(funcao, crossover, populacao, novaPopulacao));
		}else{
			return(funCrossover2p(funcao, crossover, populacao, novaPopulacao));
		}
		//-------------------------------------------------------------------------------------
	}
	/*
	 * Condição de parada, que pára o algoritmo quando forem geradas um número definido de gerações
	 */
	public static boolean condicaoParadaGer(int contadorGer){
		//Se a quantidade de gerações for maior que a definida como critério de parada retorna true, senão retorna false
		if(contadorGer != quantGerParada){
			return true;
		}else{
			return false;
		}
		//-------------------------------------------------------------------------------------
	}
	
	/*
	 * Função de Condição de Parada por Estagnação. O algoritmo irá parar no momento em que não houver melhoria
	 * na média do fitness das últimas 5 gerações
	 */
	public static boolean condicaoParadaEstag(ArrayList<Individuo> populacao){
		int minimoGeracoes = 5;
		if(listaFitnessMedio.size() < minimoGeracoes){//se ainda não houverem 5 gerações para análise
			return true;
		}else{
			int tamListaMedia = minimoGeracoes-1;
			ArrayList<Double> difFitness = new ArrayList<Double>();
			//for para calcular a diferença do fitness de um geração para a seguinte.
			//Calcula esta diferença e insere na lista difFitness
			for(int i =0;i<tamListaMedia;i++){
				difFitness.add(listaFitnessMedio.get(listaFitnessMedio.size()-tamListaMedia+i-1)-listaFitnessMedio.get(listaFitnessMedio.size()-tamListaMedia+i));
			}
			ArrayList<Double> difFitness2 = new ArrayList<Double>();
			//for para calcular a porcentagem de melhoria ou piora no fitness médio de uma geração para a seguinte
			for(int i = 0;i<tamListaMedia;i++){
				difFitness2.add(difFitness.get(i)/listaFitnessMedio.get(listaFitnessMedio.size()-tamListaMedia+i));
			}
			double media = 0;
			//for para calcular o somatório da porcentagem de melhoria entre as gerações
			for(int i = 0;i<tamListaMedia;i++){
				media += difFitness2.get(i);
			}
			double rangeNaoAceitoVariancia = 0.01;//se a média de melhoria for menor que este valor, é considerado que houve estagnação
			if(media>=-rangeNaoAceitoVariancia&& media<=rangeNaoAceitoVariancia){
				return false;
			}
		}
		return true;
	}
	
	public static boolean condicaoParada(int condParada, int contadorGer, ArrayList<Individuo> populacao){
		//Escolhe qual método de parada será invocado
		if (condParada == condParadaEstag){
			return condicaoParadaEstag(populacao);			
		}else{
			return condicaoParadaGer(contadorGer);
		}
		//------------------------------------------------------------------------------
	}
	
	public static double desvioPadrao(double []objetos) {
		//Calcula desvio padrão do objeto passado por parâmetro para a função
		if (objetos.length == 1) {
			return 0.0;
		} else {
			double mediaAritimetica = mediaAritimetica(objetos);
			double somatorio = 0;
			for (int i=0;i<objetos.length;i++){
				somatorio += Math.pow((objetos[i]-mediaAritimetica),2);
			}
				double variancia = somatorio/(objetos.length-1); 
				double desvio = Math.sqrt(variancia);
				return -desvio;
		}
		//-------------------------------------------------------------------------------
	}

	public static double mediaAritimetica(double []objetos) {
		//Calcula média aritmética do objeto passado por parâmetro para a função
		double somatorio = 0;
		for (double d : objetos) {
			somatorio += d;
		}
		return somatorio / objetos.length;
		//-------------------------------------------------------------------------------
	}
	
	/*
	 * Função principal do algoritmo genético, irá realizar a chamada dos operadores de crossover, mutação,
	 * verificação da condição de parada e troca de população
	 */
	public static Individuo executar(ArrayList<Individuo> populacao, int funcao, int crossover, int mutacao, int elitismo, int trocaPop, int condParada){
		int contadorGer = 1;
		Double mediaFitness = 0.0;
		
		//---------------------------------------------------------------------------------------------------
		//Escreve os resultados no arquivo
		//---------------------------------------------------------------------------------------------------
		System.out.println("GERACAO :"+contadorGer);
		for(int i =0;i<populacao.size();i++){
			mediaFitness += populacao.get(i).fitness;
			System.out.println("VALOR X: "+converteBinarioDouble(populacao.get(i).ind, 0, tamCromossomo/2)+" VALOR Y: "+converteBinarioDouble(populacao.get(i).ind, tamCromossomo/2, tamCromossomo)+ " FITNESS: "+populacao.get(i).fitness);
		}
		
		double[] auxDP = new double[populacao.size()];
		for(int i =0;i<populacao.size();i++){
			auxDP[i] = populacao.get(i).fitness;
		}
				
		System.out.println("Fitness Total: "+mediaFitness);
		System.out.println("Média do Fitness da População: "+mediaFitness/populacao.size());
		System.out.println("Mínimo Valor de Fitness: "+populacao.get(populacao.size()-1).fitness);
		System.out.println("Máximo Valor de Fitness: "+populacao.get(0).fitness);
		System.out.println("--------------------------------------------------------------------");
		gravarArq.printf("\nGeração: "+contadorGer);
		gravarArq.printf("\nFitness Total: "+mediaFitness);
		gravarArq.printf("\nMédia do Fitness da População: "+mediaFitness/populacao.size());
		gravarArq.printf("\nMínimo Valor de Fitness: "+populacao.get(populacao.size()-1).fitness);
		gravarArq.printf("\nMáximo Valor de Fitness: "+populacao.get(0).fitness);
		gravarArq.printf("\n--------------------------------------------------------------------");
		listaFitnessMedio.add(mediaFitness/populacao.size());
		//-------------------------------------------------------------------------------------------------------
		
		
		
		while(condicaoParada(condParada, contadorGer, populacao)){
			//Enquanto a condição de parada for true, repete o laço
			ArrayList<Individuo> novaPopulacao = new ArrayList<Individuo>();
			novaPopulacao = funElitismo(populacao,novaPopulacao, elitismo);
			
			while(novaPopulacao.size() < tamPopulacao){	
				//Enquanto o tamanho da nova população for menor que o definido, gera novo indivíduo e o insere
					novaPopulacao = funCrossover(funcao, crossover, populacao, novaPopulacao);
				//------------------------------------------------------------------------------------------------
			}
			int x = 0;
			if(elitismo == selitismo){//Se for definido usar elitismo
				x = (int) (tamPopulacao*porcElitismo);
			}
			//mutação será realizada apenas nos novos indivíduos criados, evitando gerar mutação nos indivíduos selecionados por elitismo da geração anterior
			while(x<novaPopulacao.size()){//Enquanto a porcentagem definida de operações de mutação não foi realizada
				//fica no looping fazendo mutação
					novaPopulacao = funMutacao(funcao, mutacao, populacao,contadorGer, novaPopulacao, x);
					x++;
			//----------------------------------------------------------------------------------------------------
			//----------------------------------------------------------------------------------------------------
			}
		
			
			novaPopulacao = funTrocaPop(novaPopulacao, populacao, trocaPop);//Recebe nova população de acordo com o método definido
			populacao = novaPopulacao;//Troca população
			contadorGer++;
			//---------------------------------------------------------------------------------------------------
			//Escreve os resultados no arquivo
			//---------------------------------------------------------------------------------------------------
			System.out.println("GERACAO: " +contadorGer);
			mediaFitness = 0.0;
			for(int i =0;i<populacao.size();i++){
				mediaFitness += populacao.get(i).fitness;
				System.out.println("VALOR X: "+converteBinarioDouble(populacao.get(i).ind, 0, tamCromossomo/2)+" VALOR Y: "+converteBinarioDouble(populacao.get(i).ind, tamCromossomo/2, tamCromossomo)+ " FITNESS: "+populacao.get(i).fitness);
			}
			double[] auxDP2 = new double[populacao.size()];
			for(int i =0;i<populacao.size();i++){
				auxDP2[i] = populacao.get(i).fitness;
			}
			
			System.out.println("Fitness Total: "+mediaFitness);
			System.out.println("Média do Fitness da População: "+mediaFitness/populacao.size());
			System.out.println("Mínimo Valor de Fitness: "+populacao.get(populacao.size()-1).fitness);
			System.out.println("Máximo Valor de Fitness: "+populacao.get(0).fitness);
			System.out.println("--------------------------------------------------------------------");
			gravarArq.printf("\nGeração: "+contadorGer);
			gravarArq.printf("\nFitness Total: "+mediaFitness);
			gravarArq.printf("\nMédia do Fitness da População: "+mediaFitness/populacao.size());
			gravarArq.printf("\nMínimo Valor de Fitness: "+populacao.get(populacao.size()-1).fitness);
			gravarArq.printf("\nMáximo Valor de Fitness: "+populacao.get(0).fitness);
			gravarArq.printf("\n--------------------------------------------------------------------");
			listaFitnessMedio.add(mediaFitness/populacao.size());
			//-----------------------------------------------------------------------------------------------
			
			//------------------------------------------------------------------------------------------------
		}
		return populacao.get(populacao.size()-1);
	}
	public static int converteBinarioInt(int[] arrayBinario, int inicio,int fim){
		//Converte binário em int de acordo com método passado pela professora
		int[] valor = new int[arrayBinario.length/2];
		for(int i =inicio,j = 0;i<fim;i++,j++){
			valor[j] = arrayBinario[i];
		}
		int total = 0;
		for (int i = 0; i < valor.length; i++){
			if( valor[valor.length - 1 - i] == 1 ){
				total += Math.pow(2, i);
				//System.out.println("ENTROU");
			}
		}
		//System.out.println("VALOR: "+total);
		return total;
		//-----------------------------------------------------------------------------------------------------
	}
	
	public static double converteBinarioDouble(int[] arrayBinario, int inicio, int fim) {
		//Converte binário em double de acordo com método passado pela professora
		   int valor = converteBinarioInt(arrayBinario, inicio, fim);
		   double resultado = valor*((sup-inf)/(Math.pow(2, tamCromossomo/2)-1)) + inf;
		   return resultado;
		//-----------------------------------------------------------------------------------------------------
	}
	
	public static double FitnessFGold1(int[] binario){
		//Calcula fitness da função Gold1 de acordo com o método passado pela professora
		double x = converteBinarioDouble(binario, 0, tamCromossomo/2);
		double y = converteBinarioDouble(binario, tamCromossomo/2, tamCromossomo);
		double a = 1 + (Math.pow(x+y+1, 2))*(19 - 14*x+3*(Math.pow(x, 2))-14*y+6*x*y+3*(Math.pow(y,2)));
		double b = 30+ Math.pow(2*x-3*y,2)*(18-32*x+12*Math.pow(x,2)+48*y-36*x*y+27*Math.pow(y,2));
		double z = a*b;
		z = -z;
		return (z);
		//-----------------------------------------------------------------------------------------------------
	}
	
	public static double FitnessFGold2(int[] binario){
		//Calcula fitness da função Gold2 de acordo com o método passado pela professora
		double x = converteBinarioDouble(binario, 0, tamCromossomo/2);
		double y = converteBinarioDouble(binario, tamCromossomo/2, tamCromossomo);
		double a = 1 + (Math.pow(x+y+1, 2))*(19 - 14*x+3*(Math.pow(x, 2))-14*y+6*x*y+3*(Math.pow(y,2)));
		double b = 30+ Math.pow(2*x-3*y,2)*(18-32*x+12*Math.pow(x,2)+48*y-36*x*y+27*Math.pow(y,2));
		double z = -(a*b);
		z = -z;
		return (z);
		//-----------------------------------------------------------------------------------------------------
	}
	
	public static double FitnessFBump(int[] binario){
		//Calcula fitness da função Bump de acordo com o método passado pela professora
		double x = converteBinarioDouble(binario, 0, tamCromossomo/2);
		double y = converteBinarioDouble(binario, tamCromossomo/2, tamCromossomo);
		double z;
		if (x*y < 0.75)
			z = 0;
		else if (x + y > 7.5*2)
			z = 0;
		else{
		double temp0 = Math.pow(Math.cos(x),4) + Math.pow(Math.cos(y),4);
		double temp1 = 2*(Math.pow(Math.cos(x),2))*(Math.pow(Math.cos(y),2));
		double temp2 = Math.sqrt(Math.pow(x, 2)+2*(Math.pow(y, 2)));
		z = Math.abs((temp0-temp1)/temp2);
		z = -z;
		}

		return (z);
		//-----------------------------------------------------------------------------------------------
	}
	
	public static double FitnessFRastrigin(int[] binario){
		//Calcula fitness da função Rastrigin de acordo com o método passado pela professora
		double x = converteBinarioDouble(binario, 0, tamCromossomo/2);
		double y = converteBinarioDouble(binario, tamCromossomo/2, tamCromossomo);
		double zx = (Math.pow(x, 2) - 10*Math.cos(2*Math.PI*x)+10);
		double zy = (Math.pow(y, 2) - 10*Math.cos(2*Math.PI*y)+10);
		double z = zx + zy;
		z = -z;
		return (z);
		//-----------------------------------------------------------------------------------------------
	}
	
	public static double calculaFitness(int funcao, int []binario){
		//Invoca o método correto para calcular o fitness de acordo com a função escolhida
		double fitness = 0;
		if(funcao == fgold){
			fitness = FitnessFGold1(binario);
		}
		if(funcao == fgold2){
			fitness = FitnessFGold2(binario);
		}
		if(funcao == fbump1){
			fitness = FitnessFBump(binario);
		}
		if(funcao == frastrigin){
			fitness = FitnessFRastrigin(binario);
		}
		return fitness;
		//-------------------------------------------------------------------------------------------------
	}
	
	public static Individuo criaNum(int tamCromossomo,int funcao){
		//Cria individuo setando cada indice do cromossomo com 0 e 1, aleatoriamente
		Individuo indiv = new Individuo(tamCromossomo);
		double randomNum;
		for(int i = 0;i<tamCromossomo;i++){
			randomNum = 0 + (double)(Math.random() * 1);
			if(randomNum > 0.5){
				indiv.ind[i] = 0;
			}else{
				indiv.ind[i] = 1;
			}
		}
		//--------------------------------------------------------------------------------------------------
		indiv.fitness = calculaFitness(funcao, indiv.ind);//Calcula fitness do individuo
		return indiv;//Retorna individuo
		//--------------------------------------------------------------------------------------------------
	}
	
	public static void main(String[] args) throws IOException{		
		//Caso tenha entradas insuficientes, executa de modo default
		if (args.length < 10)
		{	
			System.out.println("Entradas insuficientes. Favor seguir conforme o arquivo read.me.");
			return;
		}
		else
		{
			funcaoOtimizada = Integer.parseInt(args[0]);//Define função a ser otimizada
			porcMutacao = Double.parseDouble(args[1]);//Define porcentagem de chance de crossover, e o complemento é a porcentagem de chance de mutação
			porcElitismo = Double.parseDouble(args[2]);//Define porcentagem de elitismo			 
			quantGerParada = Integer.parseInt(args[3]);//Define quantidade de gerações para critério de parada
			tamPopulacao = Integer.parseInt(args[4]);//Define tamanho da população
			crossover = Integer.parseInt(args[5]);//Define tipo de crossver
			mutacao = Integer.parseInt(args[6]);//Define tipo de mutação
			elitismo = Integer.parseInt(args[7]);//Define se haverá elitismo
			trocaPopulacao = Integer.parseInt(args[8]);//Define tipo de troca de população
			condicaoParada = Integer.parseInt(args[9]);//Define tipo de troca de parada
			inf = Integer.parseInt(args[10]);//Define limite inferior x da função
			sup = Integer.parseInt(args[11]);//Define limite superior de y da função
		}

		
		arq = new FileWriter("ResultadosAlgoritmoGenetico.txt"); //Cria arquivo txt de saída
		gravarArq = new PrintWriter(arq);//Cria writer para arquivo txt de saída
		tamCromossomo = 16;//Define tamanho do cromossomo
		b = 5;
		
		//---------------------------------------------------------------
		//Escreve no arquivo
		//---------------------------------------------------------------
		gravarArq.printf("Parâmetros Utilizados: ");
		gravarArq.printf("\nTamanho do Cromossomo: "+tamCromossomo);
		gravarArq.printf("\nTamanho da População: "+tamPopulacao);
		if(funcaoOtimizada == fgold){
			gravarArq.printf("\nFunção Otimizada: Função Gold");
		}
		if(funcaoOtimizada == fgold2){
			gravarArq.printf("\nFunção Otimizada: Função Gold 2");
		}
		if(funcaoOtimizada == fbump1){
			gravarArq.printf("\nFunção Otimizada: Função Bump");
		}
		if(funcaoOtimizada == frastrigin){
			gravarArq.printf("\nFunção Otimizada: Função Rastrigin");
		}
		if(crossover == cross1p){
			gravarArq.printf("\nOperador de CrossOver: CrossOver de 1 ponto");
		}
		if(crossover == cross2p){
			gravarArq.printf("\nOperador de CrossOver: CrossOver de 2 pontos");
		}
		if(mutacao == mutSimples){
			gravarArq.printf("\nOperador de Mutação: Mutaçao Simples");
		}
		if(mutacao == mutNaoUniforme){
			gravarArq.printf("\nOperador de Mutação: Mutaçao Não Uniforme");
		}
		if(elitismo == selitismo){
			gravarArq.printf("\nElitismo Utilizado: Sim");
			gravarArq.printf("\nQuantidade de Indivíduos Selecionados da População Anterior via Elitismo: "+porcElitismo);
		}
		if(elitismo == nelitismo){
			gravarArq.printf("\nElitismo Utilizado: Não");
		}
		if(trocaPopulacao == trocaPopFixo){
			gravarArq.printf("\nOperador de Troca de População: Troca Imediata");
		}
		if(trocaPopulacao == trocaPopGer){
			gravarArq.printf("\nOperador de Troca de População: Troca por Inclusão");
		}
		if(condicaoParada == condParadaGer){
			gravarArq.printf("\nOperador de Condição de Parada: Para após número de "+quantGerParada+" gerações");
		}
		if(condicaoParada == condParadaEstag){
			gravarArq.printf("\nOperador de Condição de Parada: Estagnação");
		}
		
		gravarArq.printf("\nPorcentagem de Chance de Mutação: "+(porcMutacao));
		gravarArq.printf("\n--------------------------------------------------------------------");
		//----------------------------------------------------------------------
		//----------------------------------------------------------------------
		
		ArrayList<Individuo> populacao = new ArrayList<Individuo>();//Cria ArrayList de inviduos
		
		//----------------------------------------------------------------------
		//Cria a nova populacao, calcula o fitness e insere no objeto de cada individuo criado
		//----------------------------------------------------------------------
		populacao.add(criaNum(tamCromossomo,funcaoOtimizada));
		for(int i = 0;i<tamPopulacao-1;i++){
			Individuo IndivCriado = criaNum(tamCromossomo,funcaoOtimizada);
			int j = 0;
			while(j<populacao.size() && IndivCriado.fitness<populacao.get(j).fitness){
				j++;
			}
			populacao.add(j, IndivCriado);
		}
		//----------------------------------------------------------------------
		//----------------------------------------------------------------------	
		
		//----------------------------------------------------------------------
		//Executa o programa para os parâmetros escolhidos
		//----------------------------------------------------------------------
		Individuo indiv1 = executar(populacao, funcaoOtimizada, crossover, mutacao, elitismo, trocaPopulacao, condicaoParada);
		//----------------------------------------------------------------------
		//----------------------------------------------------------------------
		
		arq.close();//Encerra conexão com o arquivo
		
		//----------------------------------------------------------------------
		//Escreve na tela
		//----------------------------------------------------------------------
		System.out.println("RESULTADO FINAL:");
		System.out.println("VALOR X: "+converteBinarioDouble(indiv1.ind, 0, tamCromossomo/2)+" VALOR Y: "+converteBinarioDouble(indiv1.ind, tamCromossomo/2, tamCromossomo)+ " FITNESS: "+indiv1.fitness);
		//----------------------------------------------------------------------
		//----------------------------------------------------------------------
		
	}
}