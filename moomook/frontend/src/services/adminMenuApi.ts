export interface AdminMenu {
  id: number;
  name: string;
  price: number;
  description: string;
  tags: string[];
  spicyLevel: number;
  cookTimeMin: number;
  allergens: string[];
  ingredients: string[];
  isAvailable: boolean;
  priorityScore: number;
  popularityScore: number;
}

export interface AdminMenuRequest {
  name: string;
  price: number;
  description: string;
  tags: string[];
  spicyLevel: number;
  cookTimeMin: number;
  allergens: string[];
  ingredients: string[];
  isAvailable: boolean;
  priorityScore: number;
}

const baseUrl = "/api/admin/stores";

const handleResponse = async <T>(response: Response): Promise<T> => {
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "관리자 메뉴 API 호출에 실패했습니다.");
  }

  return response.json() as Promise<T>;
};

export const listMenus = async (storeId: number): Promise<AdminMenu[]> => {
  const response = await fetch(`${baseUrl}/${storeId}/menus`);
  return handleResponse<AdminMenu[]>(response);
};

export const createMenu = async (
  storeId: number,
  payload: AdminMenuRequest
): Promise<AdminMenu> => {
  const response = await fetch(`${baseUrl}/${storeId}/menus`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });

  return handleResponse<AdminMenu>(response);
};

export const updateMenu = async (
  storeId: number,
  menuId: number,
  payload: AdminMenuRequest
): Promise<AdminMenu> => {
  const response = await fetch(`${baseUrl}/${storeId}/menus/${menuId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });

  return handleResponse<AdminMenu>(response);
};

export const deleteMenu = async (storeId: number, menuId: number): Promise<void> => {
  const response = await fetch(`${baseUrl}/${storeId}/menus/${menuId}`, {
    method: "DELETE"
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "메뉴 삭제에 실패했습니다.");
  }
};
