import AutoCompletion from './AutoCompletion.js'

export default class GherkinAutoComplete extends AutoCompletion {
    autoComplete(word, editor){
        const searchWord = word.replaceAll(/\(|{|;/g, "")

        const ret = []
        if (searchWord == "")
            return []

        for (const key of GherkinAutoComplete.KEYWORDS) {
            if (key.toLowerCase().startsWith(searchWord.toLowerCase()) && searchWord !== key){

                ret.push({
                    text: key,
                    type: 'KEYWORD',
                    replace: () => key+" "
                })
            }
        }

        return ret
    }
}

GherkinAutoComplete.FILE_EXTENSIONS = ["feature"]
GherkinAutoComplete.KEYWORDS = ["Feature", "Rule", "Example", "Scenario", "Given", "When", "Then", "And", "But", "*", "Scenario Outline", "Scenario Template", "Examples"]
