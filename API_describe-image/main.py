from typing import Union
from fastapi import FastAPI
from dotenv import load_dotenv
import os

# Carrega as variáveis do .env
load_dotenv()

#armazena as  variáveis em um valor, caso não consiga define um valor por padrão
host = os.getenv("HOST", "127.0.0.1")  
port = int(os.getenv("PORT", 8000))    

app = FastAPI()

@app.get("/")
def read_root():
    return {"Message": "funcionando"}



if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host=host, port=port)
