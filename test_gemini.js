
async function listModels() {
  const apiKey = "AIzaSyB6KNlC2izJgPLkNeVomwpzhZbDKu3XrrM";
  const url = `https://generativelanguage.googleapis.com/v1beta/models?key=${apiKey}`;
  
  const response = await fetch(url);
  const data = await response.json();
  if (data.models) {
    console.log("Available models:");
    data.models.filter(m => m.supportedGenerationMethods.includes("generateContent")).forEach(m => console.log(m.name, m.version));
  } else {
    console.log(data);
  }
}

listModels();
