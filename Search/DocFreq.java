package Search;

public class DocFreq extends TermFeature
{
	public DocFreq(DocLanguageModel[] dinit)
	{
		for(int i=0;i<dinit.length;++i)
		{
			for(String w: dinit[i].termProb.keySet())
			{
				double c = 0;
				if(this.termWeights.containsKey(w))
				{
					c = this.termWeights.get(w);
				}
				this.termWeights.put(w,c + 1);
			}  
		}
	}
}
